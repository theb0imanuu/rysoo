package com.rysoo.infrastructure.persistence.repository;

import com.rysoo.infrastructure.persistence.entity.CvDocumentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CvDocumentRepository extends JpaRepository<CvDocumentEntity, UUID> {

    @Query("SELECT c FROM CvDocumentEntity c WHERE c.id = :id AND c.userId = :userId AND c.archivedAt IS NULL")
    Optional<CvDocumentEntity> findByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);

    @Query("SELECT c FROM CvDocumentEntity c WHERE c.userId = :userId AND c.archivedAt IS NULL ORDER BY c.createdAt DESC")
    Page<CvDocumentEntity> findAllByUserId(@Param("userId") UUID userId, Pageable pageable);

    boolean existsByIdAndUserId(UUID id, UUID userId);
}
