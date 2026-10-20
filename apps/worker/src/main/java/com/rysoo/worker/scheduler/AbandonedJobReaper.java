package com.rysoo.worker.scheduler;

import com.rysoo.infrastructure.queue.JobQueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class AbandonedJobReaper {

    private static final Logger log = LoggerFactory.getLogger(AbandonedJobReaper.class);

    private final JobQueueService jobQueueService;
    private final Duration lockTimeout;

    public AbandonedJobReaper(
            JobQueueService jobQueueService,
            @Value("${rysoo.worker.abandoned-timeout-minutes:10}") int timeoutMinutes
    ) {
        this.jobQueueService = jobQueueService;
        this.lockTimeout = Duration.ofMinutes(timeoutMinutes);
    }

    @Scheduled(fixedDelay = 60000)
    public void reap() {
        try {
            int reaped = jobQueueService.reapAbandonedJobs(lockTimeout);
            if (reaped > 0) {
                log.warn("Reaped {} abandoned jobs stuck in PROCESSING beyond {}", reaped, lockTimeout);
            }
        } catch (Exception e) {
            log.error("Exception during abandoned job reaping: {}", e.getMessage(), e);
        }
    }
}
