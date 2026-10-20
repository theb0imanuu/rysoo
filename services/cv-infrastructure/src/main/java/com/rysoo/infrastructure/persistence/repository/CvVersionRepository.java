package com.rysoo.infrastructure.persistence.repository;

import com.rysoo.infrastructure.persistence.entity.CvVersionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CvVersionRepository extends JpaRepository<CvVersionEntity, UUID> {

    @Query("SELECT v FROM CvVersionEntity v WHERE v.cvDocument.id = :cvDocumentId ORDER BY v.versionNumber DESC")
    List<CvVersionEntity> findAllByCvDocumentId(@Param("cvDocumentId") UUID cvDocumentId);

    @Query("SELECT v FROM CvVersionEntity v WHERE v.cvDocument.id = :cvDocumentId AND v.versionNumber = :versionNumber")
    Optional<CvVersionEntity> findByCvDocumentIdAndVersionNumber(@Param("cvDocumentId") UUID cvDocumentId, @Param("versionNumber") int versionNumber);

    @Query("SELECT MAX(v.versionNumber) FROM CvVersionEntity v WHERE v.cvDocument.id = :cvDocumentId")
    Optional<Integer> findMaxVersionNumberByCvDocumentId(@Param("cvDocumentId") UUID cvDocumentId);
}
