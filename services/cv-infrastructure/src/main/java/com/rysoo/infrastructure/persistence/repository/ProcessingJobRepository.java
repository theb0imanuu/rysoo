package com.rysoo.infrastructure.persistence.repository;

import com.rysoo.infrastructure.persistence.entity.ProcessingJobEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProcessingJobRepository extends JpaRepository<ProcessingJobEntity, UUID> {

    Optional<ProcessingJobEntity> findByIdempotencyKey(String idempotencyKey);

    @Query("SELECT j FROM ProcessingJobEntity j WHERE j.status IN ('QUEUED', 'RETRYING') AND j.nextRunAt <= :now ORDER BY j.priority DESC, j.createdAt ASC")
    List<ProcessingJobEntity> findAvailableJobsForWorker(@Param("now") Instant now, Pageable pageable);

    @Query("SELECT j FROM ProcessingJobEntity j WHERE j.status = 'PROCESSING' AND j.lockedAt <= :threshold")
    List<ProcessingJobEntity> findAbandonedJobs(@Param("threshold") Instant threshold);

    @Query("SELECT j FROM ProcessingJobEntity j WHERE j.id = :id AND j.userId = :userId")
    Optional<ProcessingJobEntity> findByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);
}
