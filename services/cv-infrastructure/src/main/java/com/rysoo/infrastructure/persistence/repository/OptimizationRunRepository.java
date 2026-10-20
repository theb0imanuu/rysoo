package com.rysoo.infrastructure.persistence.repository;

import com.rysoo.infrastructure.persistence.entity.OptimizationRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OptimizationRunRepository extends JpaRepository<OptimizationRunEntity, UUID> {

    @Query("SELECT r FROM OptimizationRunEntity r WHERE r.project.id = :projectId ORDER BY r.createdAt DESC")
    List<OptimizationRunEntity> findAllByProjectId(@Param("projectId") UUID projectId);

    @Query("SELECT r FROM OptimizationRunEntity r WHERE r.id = :runId AND r.project.userId = :userId")
    Optional<OptimizationRunEntity> findByIdAndUserId(@Param("runId") UUID runId, @Param("userId") UUID userId);

    List<OptimizationRunEntity> findByStatus(String status);
}
