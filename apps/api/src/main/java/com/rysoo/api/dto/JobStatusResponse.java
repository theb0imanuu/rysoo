package com.rysoo.api.dto;

import java.time.Instant;
import java.util.UUID;

public record JobStatusResponse(
    UUID jobId,
    String jobType,
    String status,
    int attemptCount,
    int maxRetries,
    String idempotencyKey,
    String resultJson,
    String errorMessage,
    Instant createdAt,
    Instant updatedAt
) {}
