package com.rysoo.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rysoo.infrastructure.persistence.entity.ProcessingJobEntity;
import com.rysoo.infrastructure.persistence.entity.ProfileEntity;
import com.rysoo.infrastructure.persistence.repository.ProcessingJobRepository;
import com.rysoo.infrastructure.persistence.repository.ProfileRepository;
import com.rysoo.infrastructure.queue.JobQueueService;
import com.rysoo.worker.service.JobProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class RysooWorkerIntegrationTest {

    @Autowired
    private JobQueueService jobQueueService;

    @Autowired
    private JobProcessor jobProcessor;

    @Autowired
    private ProcessingJobRepository jobRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        profileRepository.save(new ProfileEntity(userId, "worker.e2e@example.com", "E2E Worker User"));
    }

    @Test
    @DisplayName("Worker should process enqueued CV_OPTIMIZATION job and transition to COMPLETED")
    void testWorkerProcessesOptimizationJob() throws Exception {
        Map<String, Object> profilePayload = Map.of(
                "candidateName", "Alex Rivera",
                "email", "alex@example.com",
                "phone", "555-0144",
                "location", "New York, NY",
                "summary", "Senior backend engineer with deep Java experience.",
                "employers", Set.of("Acme Corp", "TechWave"),
                "degrees", Set.of("BS in Computer Science - Columbia University"),
                "skills", Set.of("Java", "Spring Boot", "PostgreSQL", "Docker")
        );

        Map<String, Object> payload = Map.of(
                "jobDescription", "Position: Senior Backend Engineer\nLooking for an experienced engineer with Java, Spring Boot, and PostgreSQL.",
                "profile", profilePayload
        );

        String payloadJson = objectMapper.writeValueAsString(payload);
        String idempotencyKey = "worker-e2e-" + UUID.randomUUID();

        // 1. Enqueue job
        ProcessingJobEntity job = jobQueueService.enqueue(userId, "CV_OPTIMIZATION", payloadJson, idempotencyKey, 1);
        assertThat(job.getStatus()).isEqualTo("QUEUED");

        // 2. Poll and Lock
        List<ProcessingJobEntity> locked = jobQueueService.pollAndLockJobs(5, "worker-test-agent");
        assertThat(locked).hasSize(1);
        assertThat(locked.get(0).getStatus()).isEqualTo("PROCESSING");

        // 3. Process
        jobProcessor.processJob(locked.get(0));

        // 4. Verify completed
        ProcessingJobEntity completed = jobRepository.findById(job.getId()).orElseThrow();
        assertThat(completed.getStatus()).isEqualTo("COMPLETED");
        assertThat(completed.getResultJson()).isNotBlank();
        assertThat(completed.getResultJson()).contains("Alex Rivera");
        assertThat(completed.getResultJson()).contains("tailoredMarkdown");
        assertThat(completed.getLockedAt()).isNull();
        assertThat(completed.getLockedBy()).isNull();
    }
}
