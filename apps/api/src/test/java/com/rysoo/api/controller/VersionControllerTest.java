package com.rysoo.api.controller;

import com.rysoo.infrastructure.persistence.entity.CvDocumentEntity;
import com.rysoo.infrastructure.persistence.entity.CvVersionEntity;
import com.rysoo.infrastructure.persistence.entity.ProfileEntity;
import com.rysoo.infrastructure.persistence.repository.CvDocumentRepository;
import com.rysoo.infrastructure.persistence.repository.CvVersionRepository;
import com.rysoo.infrastructure.persistence.repository.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VersionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private CvDocumentRepository documentRepository;

    @Autowired
    private CvVersionRepository versionRepository;

    private ProfileEntity owner;
    private CvDocumentEntity document;
    private CvVersionEntity version1;
    private CvVersionEntity version2;

    @BeforeEach
    void setUp() {
        versionRepository.deleteAll();
        documentRepository.deleteAll();
        profileRepository.deleteAll();

        owner = profileRepository.save(new ProfileEntity(UUID.randomUUID(), "owner@example.com", "Owner Name"));

        document = new CvDocumentEntity(owner.getId(), "Main CV");
        document = documentRepository.save(document);

        version1 = new CvVersionEntity();
        version1.setCvDocument(document);
        version1.setVersionNumber(1);
        version1.setVersionType("ORIGINAL_UPLOAD");
        version1.setNormalizedMarkdown("# Original CV");
        version1.setContentHashSha256("hash1");
        version1.setFileName("cv_v1.pdf");
        version1 = versionRepository.save(version1);

        version2 = new CvVersionEntity();
        version2.setCvDocument(document);
        version2.setVersionNumber(2);
        version2.setVersionType("OPTIMIZED");
        version2.setNormalizedMarkdown("# Tailored CV");
        version2.setContentHashSha256("hash2");
        version2.setFileName("cv_v2.pdf");
        version2 = versionRepository.save(version2);
    }

    @Test
    void getVersions_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/cv/" + document.getId() + "/versions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getVersions_owner_returnsAllVersions() throws Exception {
        mockMvc.perform(get("/api/v1/cv/" + document.getId() + "/versions")
                        .with(jwt().jwt(j -> j.subject(owner.getId().toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void getVersions_stranger_returns404() throws Exception {
        UUID strangerId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/cv/" + document.getId() + "/versions")
                        .with(jwt().jwt(j -> j.subject(strangerId.toString()))))
                .andExpect(status().isNotFound());
    }

    @Test
    void rollbackToVersion_owner_rollsBackToVersion1() throws Exception {
        mockMvc.perform(post("/api/v1/cv/" + document.getId() + "/versions/1/rollback")
                        .with(jwt().jwt(j -> j.subject(owner.getId().toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.versionNumber").value(3))
                .andExpect(jsonPath("$.data.versionType").value("ROLLED_BACK_TO_V1"))
                .andExpect(jsonPath("$.data.normalizedMarkdown").value("# Original CV"));
    }
}
