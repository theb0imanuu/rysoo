package com.rysoo.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record EnqueueOptimizationRequest(
    @NotNull(message = "cvDocumentId is required")
    UUID cvDocumentId,

    @NotNull(message = "jobDescriptionId is required")
    UUID jobDescriptionId,

    @NotBlank(message = "idempotencyKey is required")
    String idempotencyKey
) {}
