package com.rysoo.api.dto;

import java.time.Instant;
import java.util.UUID;

public record CvVersionResponse(
        UUID id,
        UUID cvDocumentId,
        int versionNumber,
        String versionType,
        String fileName,
        String normalizedMarkdown,
        Instant createdAt
) {
}
