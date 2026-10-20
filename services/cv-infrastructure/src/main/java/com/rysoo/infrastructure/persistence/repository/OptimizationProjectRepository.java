package com.rysoo.infrastructure.persistence.repository;

import com.rysoo.infrastructure.persistence.entity.OptimizationProjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OptimizationProjectRepository extends JpaRepository<OptimizationProjectEntity, UUID> {

    @Query("SELECT p FROM OptimizationProjectEntity p WHERE p.id = :id AND p.userId = :userId")
    Optional<OptimizationProjectEntity> findByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);

    @Query("SELECT p FROM OptimizationProjectEntity p WHERE p.userId = :userId ORDER BY p.createdAt DESC")
    Page<OptimizationProjectEntity> findAllByUserId(@Param("userId") UUID userId, Pageable pageable);
}
