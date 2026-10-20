package com.rysoo.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rysoo.api.dto.EnqueueOptimizationRequest;
import com.rysoo.infrastructure.persistence.entity.ProcessingJobEntity;
import com.rysoo.infrastructure.persistence.entity.ProfileEntity;
import com.rysoo.infrastructure.persistence.repository.ProcessingJobRepository;
import com.rysoo.infrastructure.persistence.repository.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ProcessingJobRepository jobRepository;

    private UUID ownerId;
    private UUID strangerId;

    @BeforeEach
    void setUp() {
        jobRepository.deleteAll();
        profileRepository.deleteAll();

        ownerId = UUID.randomUUID();
        strangerId = UUID.randomUUID();

        profileRepository.save(new ProfileEntity(ownerId, "owner-" + ownerId + "@example.com", "Job Owner"));
        profileRepository.save(new ProfileEntity(strangerId, "stranger-" + strangerId + "@example.com", "Job Stranger"));
    }

    @Test
    @DisplayName("Enqueue optimization job requires authentication")
    void enqueueJobRequiresAuth() throws Exception {
        EnqueueOptimizationRequest request = new EnqueueOptimizationRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "test-idem-key"
        );

        mockMvc.perform(post("/api/v1/jobs/cv-optimization")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Enqueue optimization job succeeds with JWT and returns 202 ACCEPTED")
    void enqueueJobSucceedsWithJwt() throws Exception {
        UUID cvDocId = UUID.randomUUID();
        UUID jdId = UUID.randomUUID();
        String idemKey = "idem-" + UUID.randomUUID();

        EnqueueOptimizationRequest request = new EnqueueOptimizationRequest(cvDocId, jdId, idemKey);

        mockMvc.perform(post("/api/v1/jobs/cv-optimization")
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(jwt -> jwt.subject(ownerId.toString()).claim("email", "owner@example.com")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("QUEUED")))
                .andExpect(jsonPath("$.data.idempotencyKey", is(idemKey)));
    }

    @Test
    @DisplayName("Should prevent stranger from accessing another user's job (404)")
    void strangerCannotAccessJob() throws Exception {
        ProcessingJobEntity job = new ProcessingJobEntity(
                "CV_OPTIMIZATION",
                ownerId,
                "{}",
                "key-" + UUID.randomUUID()
        );
        job = jobRepository.save(job);

        // Owner can access
        mockMvc.perform(get("/api/v1/jobs/" + job.getId())
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(jwt -> jwt.subject(ownerId.toString()).claim("email", "owner@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("QUEUED")));

        // Stranger gets 404
        mockMvc.perform(get("/api/v1/jobs/" + job.getId())
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(jwt -> jwt.subject(strangerId.toString()).claim("email", "stranger@example.com"))))
                .andExpect(status().isNotFound());
    }
}
