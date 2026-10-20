package com.rysoo.worker.scheduler;

import com.rysoo.infrastructure.persistence.entity.ProcessingJobEntity;
import com.rysoo.infrastructure.queue.JobQueueService;
import com.rysoo.worker.service.JobProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class QueuePoller {

    private static final Logger log = LoggerFactory.getLogger(QueuePoller.class);

    private final JobQueueService jobQueueService;
    private final JobProcessor jobProcessor;
    private final String workerId;
    private final int batchSize;

    public QueuePoller(
            JobQueueService jobQueueService,
            JobProcessor jobProcessor,
            @Value("${rysoo.worker.id:worker-primary}") String workerId,
            @Value("${rysoo.worker.batch-size:5}") int batchSize
    ) {
        this.jobQueueService = jobQueueService;
        this.jobProcessor = jobProcessor;
        this.workerId = workerId;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${rysoo.worker.poll-interval-ms:2000}")
    public void pollQueue() {
        try {
            List<ProcessingJobEntity> lockedJobs = jobQueueService.pollAndLockJobs(batchSize, workerId);
            if (!lockedJobs.isEmpty()) {
                log.info("Worker {} locked {} jobs for processing.", workerId, lockedJobs.size());
                for (ProcessingJobEntity job : lockedJobs) {
                    jobProcessor.processJob(job);
                }
            }
        } catch (Exception e) {
            log.error("Exception occurred during queue polling: {}", e.getMessage(), e);
        }
    }
}
