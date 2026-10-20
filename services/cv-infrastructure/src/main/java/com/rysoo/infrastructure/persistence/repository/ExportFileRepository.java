package com.rysoo.infrastructure.persistence.repository;

import com.rysoo.infrastructure.persistence.entity.ExportFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExportFileRepository extends JpaRepository<ExportFileEntity, UUID> {

    @Query("SELECT e FROM ExportFileEntity e WHERE e.id = :id AND e.userId = :userId")
    Optional<ExportFileEntity> findByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);

    @Query("SELECT e FROM ExportFileEntity e WHERE e.userId = :userId ORDER BY e.createdAt DESC")
    List<ExportFileEntity> findAllByUserId(@Param("userId") UUID userId);
}
