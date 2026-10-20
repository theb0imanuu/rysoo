package com.rysoo.infrastructure.persistence.repository;

import com.rysoo.infrastructure.persistence.entity.JobDescriptionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JobDescriptionRepository extends JpaRepository<JobDescriptionEntity, UUID> {

    @Query("SELECT j FROM JobDescriptionEntity j WHERE j.id = :id AND j.userId = :userId")
    Optional<JobDescriptionEntity> findByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);

    @Query("SELECT j FROM JobDescriptionEntity j WHERE j.userId = :userId ORDER BY j.createdAt DESC")
    Page<JobDescriptionEntity> findAllByUserId(@Param("userId") UUID userId, Pageable pageable);
}
