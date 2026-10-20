package com.rysoo.infrastructure.queue;

import com.rysoo.infrastructure.persistence.entity.ProcessingJobEntity;
import com.rysoo.infrastructure.persistence.entity.ProfileEntity;
import com.rysoo.infrastructure.persistence.repository.ProcessingJobRepository;
import com.rysoo.infrastructure.persistence.repository.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JobQueueService.class)
@ActiveProfiles("test")
class JobQueueServiceTest {

    @Autowired
    private JobQueueService jobQueueService;

    @Autowired
    private ProcessingJobRepository jobRepository;

    @Autowired
    private ProfileRepository profileRepository;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        ProfileEntity profile = new ProfileEntity(userId, "worker.test@example.com", "Worker Test User");
        profileRepository.save(profile);
    }

    @Test
    @DisplayName("Should enforce idempotency by returning existing job on duplicate key")
    void testIdempotencyOnEnqueue() {
        String idempotencyKey = "idem-key-" + UUID.randomUUID();

        ProcessingJobEntity job1 = jobQueueService.enqueue(userId, "CV_OPTIMIZATION", "{\"test\":1}", idempotencyKey, 1);
        ProcessingJobEntity job2 = jobQueueService.enqueue(userId, "CV_OPTIMIZATION", "{\"test\":2}", idempotencyKey, 1);

        assertThat(job1.getId()).isNotNull();
        assertThat(job2.getId()).isEqualTo(job1.getId());
        assertThat(jobRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should poll, lock, and transition jobs through states")
    void testPollLockAndCompleteJob() {
        String idempotencyKey = "job-" + UUID.randomUUID();
        ProcessingJobEntity job = jobQueueService.enqueue(userId, "CV_OPTIMIZATION", "{\"payload\":\"data\"}", idempotencyKey, 5);

        // 1. Poll and Lock
        List<ProcessingJobEntity> lockedJobs = jobQueueService.pollAndLockJobs(5, "worker-node-1");
        assertThat(lockedJobs).hasSize(1);
        ProcessingJobEntity locked = lockedJobs.get(0);
        assertThat(locked.getId()).isEqualTo(job.getId());
        assertThat(locked.getStatus()).isEqualTo("PROCESSING");
        assertThat(locked.getLockedBy()).isEqualTo("worker-node-1");
        assertThat(locked.getAttemptCount()).isEqualTo(1);

        // 2. Complete Job
        String resultJson = "{\"tailoredMarkdown\":\"# Done\"}";
        jobQueueService.markCompleted(job.getId(), resultJson);

        Optional<ProcessingJobEntity> completedJob = jobRepository.findById(job.getId());
        assertThat(completedJob).isPresent();
        assertThat(completedJob.get().getStatus()).isEqualTo("COMPLETED");
        assertThat(completedJob.get().getResultJson()).isEqualTo(resultJson);
        assertThat(completedJob.get().getLockedAt()).isNull();
        assertThat(completedJob.get().getLockedBy()).isNull();
    }

    @Test
    @DisplayName("Should handle failure with retry and exponential backoff, then permanent failure")
    void testFailureRetryAndMaxAttempts() {
        String key = "fail-job-" + UUID.randomUUID();
        ProcessingJobEntity job = jobQueueService.enqueue(userId, "CV_OPTIMIZATION", "{}", key, 1);

        // Simulate 1st failure
        job.setAttemptCount(1);
        jobRepository.save(job);
        jobQueueService.markFailed(job.getId(), "Transient timeout error");

        ProcessingJobEntity retrying = jobRepository.findById(job.getId()).orElseThrow();
        assertThat(retrying.getStatus()).isEqualTo("RETRYING");
        assertThat(retrying.getErrorMessage()).contains("Transient timeout error");
        assertThat(retrying.getNextRunAt()).isAfter(Instant.now());

        // Simulate 3rd failure (exceeding max retries of 3)
        retrying.setAttemptCount(3);
        jobRepository.save(retrying);
        jobQueueService.markFailed(job.getId(), "Permanent external service failure");

        ProcessingJobEntity failed = jobRepository.findById(job.getId()).orElseThrow();
        assertThat(failed.getStatus()).isEqualTo("FAILED");
        assertThat(failed.getErrorMessage()).contains("Permanent external service failure");
    }

    @Test
    @DisplayName("Should reap abandoned jobs stuck in PROCESSING beyond threshold")
    void testReapAbandonedJobs() {
        String key = "stuck-job-" + UUID.randomUUID();
        ProcessingJobEntity job = jobQueueService.enqueue(userId, "CV_OPTIMIZATION", "{}", key, 1);

        // Simulate stuck in PROCESSING 15 minutes ago
        job.setStatus("PROCESSING");
        job.setLockedAt(Instant.now().minus(Duration.ofMinutes(15)));
        job.setLockedBy("crashed-worker");
        job.setAttemptCount(1);
        jobRepository.save(job);

        // Reap jobs abandoned > 10 minutes
        int reapedCount = jobQueueService.reapAbandonedJobs(Duration.ofMinutes(10));
        assertThat(reapedCount).isEqualTo(1);

        ProcessingJobEntity recovered = jobRepository.findById(job.getId()).orElseThrow();
        assertThat(recovered.getStatus()).isEqualTo("RETRYING");
        assertThat(recovered.getLockedAt()).isNull();
        assertThat(recovered.getLockedBy()).isNull();
    }
}
