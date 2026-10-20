package com.rysoo.infrastructure.queue;

import com.rysoo.infrastructure.persistence.entity.ProcessingJobEntity;
import com.rysoo.infrastructure.persistence.repository.ProcessingJobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class JobQueueService {

    private static final Logger log = LoggerFactory.getLogger(JobQueueService.class);

    private final ProcessingJobRepository jobRepository;

    public JobQueueService(ProcessingJobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Transactional
    public ProcessingJobEntity enqueue(UUID userId, String jobType, String payloadJson, String idempotencyKey, int priority) {
        // Enforce idempotency: return existing job if key already registered
        Optional<ProcessingJobEntity> existing = jobRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            log.info("Idempotent submission detected for key: {}. Returning existing job: {}", idempotencyKey, existing.get().getId());
            return existing.get();
        }

        ProcessingJobEntity job = new ProcessingJobEntity(jobType, userId, payloadJson, idempotencyKey);
        job.setPriority(priority);
        job.setStatus("QUEUED");
        return jobRepository.save(job);
    }

    @Transactional
    public List<ProcessingJobEntity> pollAndLockJobs(int batchSize, String workerId) {
        Instant now = Instant.now().plusSeconds(1);
        List<ProcessingJobEntity> available = jobRepository.findAvailableJobsForWorker(now, PageRequest.of(0, batchSize));
        List<ProcessingJobEntity> locked = new ArrayList<>();

        for (ProcessingJobEntity job : available) {
            job.setStatus("PROCESSING");
            job.setLockedAt(now);
            job.setLockedBy(workerId);
            job.setAttemptCount(job.getAttemptCount() + 1);
            job.setUpdatedAt(now);
            locked.add(jobRepository.save(job));
        }

        return locked;
    }

    @Transactional
    public void markCompleted(UUID jobId, String resultJson) {
        jobRepository.findById(jobId).ifPresent(job -> {
            job.setStatus("COMPLETED");
            job.setResultJson(resultJson);
            job.setLockedAt(null);
            job.setLockedBy(null);
            job.setUpdatedAt(Instant.now());
            jobRepository.save(job);
            log.info("Job {} completed successfully.", jobId);
        });
    }

    @Transactional
    public void markFailed(UUID jobId, String errorMessage) {
        jobRepository.findById(jobId).ifPresent(job -> {
            Instant now = Instant.now();
            job.setErrorMessage(errorMessage);
            job.setLockedAt(null);
            job.setLockedBy(null);
            job.setUpdatedAt(now);

            if (job.getAttemptCount() < job.getMaxRetries()) {
                // Exponential backoff: 2^attempt * 10 seconds
                long delaySeconds = (long) Math.pow(2, job.getAttemptCount()) * 10L;
                job.setStatus("RETRYING");
                job.setNextRunAt(now.plusSeconds(delaySeconds));
                log.warn("Job {} failed with attempt {}/{}. Scheduled retry in {}s. Error: {}",
                        jobId, job.getAttemptCount(), job.getMaxRetries(), delaySeconds, errorMessage);
            } else {
                job.setStatus("FAILED");
                log.error("Job {} failed permanently after {} attempts. Error: {}",
                        jobId, job.getAttemptCount(), errorMessage);
            }
            jobRepository.save(job);
        });
    }

    @Transactional
    public int reapAbandonedJobs(Duration lockTimeout) {
        Instant threshold = Instant.now().minus(lockTimeout);
        List<ProcessingJobEntity> abandoned = jobRepository.findAbandonedJobs(threshold);
        int reapedCount = 0;

        for (ProcessingJobEntity job : abandoned) {
            Instant now = Instant.now();
            job.setLockedAt(null);
            job.setLockedBy(null);
            job.setUpdatedAt(now);

            if (job.getAttemptCount() < job.getMaxRetries()) {
                job.setStatus("RETRYING");
                job.setNextRunAt(now); // immediate retry
                job.setErrorMessage("Reaped after worker inactivity timeout.");
                log.warn("Reaped abandoned job {}. Reset to RETRYING.", job.getId());
            } else {
                job.setStatus("FAILED");
                job.setErrorMessage("Job abandoned and exceeded max retries (" + job.getMaxRetries() + ").");
                log.error("Reaped abandoned job {}. Marked permanently FAILED.", job.getId());
            }
            jobRepository.save(job);
            reapedCount++;
        }

        return reapedCount;
    }

    @Transactional(readOnly = true)
    public Optional<ProcessingJobEntity> getJob(UUID jobId, UUID userId) {
        return jobRepository.findByIdAndUserId(jobId, userId);
    }

    @Transactional(readOnly = true)
    public Optional<ProcessingJobEntity> getJobByIdempotencyKey(String idempotencyKey, UUID userId) {
        return jobRepository.findByIdempotencyKey(idempotencyKey)
                .filter(job -> job.getUserId().equals(userId));
    }
}
