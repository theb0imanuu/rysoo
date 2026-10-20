package com.rysoo.infrastructure.persistence.repository;

import com.rysoo.infrastructure.persistence.entity.UsageLedgerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UsageLedgerRepository extends JpaRepository<UsageLedgerEntity, UUID> {

    @Query("SELECT u FROM UsageLedgerEntity u WHERE u.userId = :userId ORDER BY u.createdAt DESC")
    List<UsageLedgerEntity> findAllByUserId(@Param("userId") UUID userId);
}
