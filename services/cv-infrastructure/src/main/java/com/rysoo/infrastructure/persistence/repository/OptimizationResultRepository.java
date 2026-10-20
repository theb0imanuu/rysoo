package com.rysoo.infrastructure.persistence.repository;

import com.rysoo.infrastructure.persistence.entity.OptimizationResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OptimizationResultRepository extends JpaRepository<OptimizationResultEntity, UUID> {

    @Query("SELECT res FROM OptimizationResultEntity res WHERE res.optimizationRun.id = :runId")
    Optional<OptimizationResultEntity> findByOptimizationRunId(@Param("runId") UUID runId);

    @Query("SELECT res FROM OptimizationResultEntity res WHERE res.id = :id AND res.optimizationRun.project.userId = :userId")
    Optional<OptimizationResultEntity> findByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);
}
