package com.rysoo.infrastructure.persistence;

import com.rysoo.infrastructure.persistence.entity.CvDocumentEntity;
import com.rysoo.infrastructure.persistence.entity.CvVersionEntity;
import com.rysoo.infrastructure.persistence.entity.ProfileEntity;
import com.rysoo.infrastructure.persistence.repository.CvDocumentRepository;
import com.rysoo.infrastructure.persistence.repository.CvVersionRepository;
import com.rysoo.infrastructure.persistence.repository.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CvDocumentRepositoryTest {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private CvDocumentRepository cvDocumentRepository;

    @Autowired
    private CvVersionRepository cvVersionRepository;

    private UUID ownerId;
    private UUID strangerId;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();
        strangerId = UUID.randomUUID();

        ProfileEntity owner = new ProfileEntity(ownerId, "owner@example.com", "Owner User");
        ProfileEntity stranger = new ProfileEntity(strangerId, "stranger@example.com", "Stranger User");

        profileRepository.save(owner);
        profileRepository.save(stranger);
    }

    @Test
    @DisplayName("Owner can retrieve their own CV document with ownership check")
    void ownerCanRetrieveCvDocument() {
        CvDocumentEntity doc = new CvDocumentEntity(ownerId, "Software Engineer CV");
        CvDocumentEntity saved = cvDocumentRepository.save(doc);

        Optional<CvDocumentEntity> retrieved = cvDocumentRepository.findByIdAndUserId(saved.getId(), ownerId);
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getTitle()).isEqualTo("Software Engineer CV");
    }

    @Test
    @DisplayName("Stranger cannot access another user's CV document (Option A ownership enforcement)")
    void strangerCannotAccessCvDocument() {
        CvDocumentEntity doc = new CvDocumentEntity(ownerId, "Confidential Resume");
        CvDocumentEntity saved = cvDocumentRepository.save(doc);

        Optional<CvDocumentEntity> retrieved = cvDocumentRepository.findByIdAndUserId(saved.getId(), strangerId);
        assertThat(retrieved).isEmpty();
    }

    @Test
    @DisplayName("Deleting CV document cascades to its versions")
    void deletingDocumentCascadesToVersions() {
        CvDocumentEntity doc = new CvDocumentEntity(ownerId, "Multi-version CV");
        
        CvVersionEntity v1 = new CvVersionEntity();
        v1.setVersionNumber(1);
        v1.setVersionType("ORIGINAL_UPLOAD");
        v1.setContentHashSha256("abc123hash");
        v1.setNormalizedMarkdown("# Original");
        doc.addVersion(v1);

        CvDocumentEntity savedDoc = cvDocumentRepository.save(doc);
        UUID docId = savedDoc.getId();

        assertThat(cvVersionRepository.findAllByCvDocumentId(docId)).hasSize(1);

        // Delete document
        cvDocumentRepository.delete(savedDoc);
        cvDocumentRepository.flush();

        // Verify versions are cascade deleted
        assertThat(cvVersionRepository.findAllByCvDocumentId(docId)).isEmpty();
    }
}
