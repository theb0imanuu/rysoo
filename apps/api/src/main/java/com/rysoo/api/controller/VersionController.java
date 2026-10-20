package com.rysoo.api.controller;

import com.rysoo.api.dto.ApiResponse;
import com.rysoo.api.dto.CvVersionResponse;
import com.rysoo.infrastructure.persistence.entity.CvDocumentEntity;
import com.rysoo.infrastructure.persistence.entity.CvVersionEntity;
import com.rysoo.infrastructure.persistence.repository.CvDocumentRepository;
import com.rysoo.infrastructure.persistence.repository.CvVersionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cv/{cvDocumentId}/versions")
@Tag(name = "CV Version History", description = "Endpoints for tracking and rolling back CV document versions")
public class VersionController {

    private static final Logger log = LoggerFactory.getLogger(VersionController.class);

    private final CvDocumentRepository documentRepository;
    private final CvVersionRepository versionRepository;

    public VersionController(CvDocumentRepository documentRepository, CvVersionRepository versionRepository) {
        this.documentRepository = documentRepository;
        this.versionRepository = versionRepository;
    }

    @GetMapping
    @Operation(summary = "List all versions for a CV document")
    public ResponseEntity<ApiResponse<List<CvVersionResponse>>> getVersions(
            @PathVariable UUID cvDocumentId,
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {

        UUID userId = UUID.fromString(jwt.getSubject());
        CvDocumentEntity doc = getOwnedDocumentOrThrow(cvDocumentId, userId);

        List<CvVersionResponse> versions = versionRepository.findAllByCvDocumentId(doc.getId())
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.ok(versions, correlationId));
    }

    @GetMapping("/{versionNumber}")
    @Operation(summary = "Get specific version details")
    public ResponseEntity<ApiResponse<CvVersionResponse>> getVersion(
            @PathVariable UUID cvDocumentId,
            @PathVariable int versionNumber,
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {

        UUID userId = UUID.fromString(jwt.getSubject());
        CvDocumentEntity doc = getOwnedDocumentOrThrow(cvDocumentId, userId);

        CvVersionEntity version = versionRepository.findByCvDocumentIdAndVersionNumber(doc.getId(), versionNumber)
                .orElseThrow(() -> new com.rysoo.api.exception.ResourceNotFoundException("Version not found"));

        return ResponseEntity.ok(ApiResponse.ok(toResponse(version), correlationId));
    }

    @PostMapping("/{versionNumber}/rollback")
    @Transactional
    @Operation(summary = "Roll back CV document to a previous version (creates new audit version)")
    public ResponseEntity<ApiResponse<CvVersionResponse>> rollbackToVersion(
            @PathVariable UUID cvDocumentId,
            @PathVariable int versionNumber,
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {

        UUID userId = UUID.fromString(jwt.getSubject());
        CvDocumentEntity doc = getOwnedDocumentOrThrow(cvDocumentId, userId);

        CvVersionEntity targetVersion = versionRepository.findByCvDocumentIdAndVersionNumber(doc.getId(), versionNumber)
                .orElseThrow(() -> new com.rysoo.api.exception.ResourceNotFoundException("Version not found"));

        int nextVersion = versionRepository.findMaxVersionNumberByCvDocumentId(doc.getId()).orElse(versionNumber) + 1;

        CvVersionEntity rolledBackVersion = new CvVersionEntity();
        rolledBackVersion.setCvDocument(doc);
        rolledBackVersion.setVersionNumber(nextVersion);
        rolledBackVersion.setVersionType("ROLLED_BACK_TO_V" + versionNumber);
        rolledBackVersion.setContentHashSha256(targetVersion.getContentHashSha256() != null ? targetVersion.getContentHashSha256() : "hash-v" + nextVersion);
        rolledBackVersion.setNormalizedMarkdown(targetVersion.getNormalizedMarkdown());
        rolledBackVersion.setFileName(targetVersion.getFileName());
        rolledBackVersion = versionRepository.save(rolledBackVersion);

        doc.setUpdatedAt(Instant.now());
        documentRepository.save(doc);

        log.info("User {} rolled back CV {} from v{} to new v{}", userId, cvDocumentId, versionNumber, nextVersion);

        return ResponseEntity.ok(ApiResponse.ok(toResponse(rolledBackVersion), correlationId));
    }

    private CvDocumentEntity getOwnedDocumentOrThrow(UUID cvDocumentId, UUID userId) {
        CvDocumentEntity doc = documentRepository.findById(cvDocumentId)
                .orElseThrow(() -> new com.rysoo.api.exception.ResourceNotFoundException("Document not found"));

        if (!doc.getUserId().equals(userId)) {
            // Strict tenant isolation: do not disclose existence
            throw new com.rysoo.api.exception.ResourceNotFoundException("Document not found");
        }
        return doc;
    }

    private CvVersionResponse toResponse(CvVersionEntity entity) {
        return new CvVersionResponse(
                entity.getId(),
                entity.getCvDocument().getId(),
                entity.getVersionNumber(),
                entity.getVersionType(),
                entity.getFileName(),
                entity.getNormalizedMarkdown(),
                entity.getCreatedAt()
        );
    }
}
