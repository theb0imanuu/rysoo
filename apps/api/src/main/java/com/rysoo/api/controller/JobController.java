package com.rysoo.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rysoo.api.dto.ApiResponse;
import com.rysoo.api.dto.EnqueueOptimizationRequest;
import com.rysoo.api.dto.JobStatusResponse;
import com.rysoo.api.security.AuthenticatedUser;
import com.rysoo.infrastructure.persistence.entity.ProcessingJobEntity;
import com.rysoo.infrastructure.queue.JobQueueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/jobs")
@Tag(name = "Jobs", description = "Durable background processing job management and status polling")
public class JobController {

    private final JobQueueService jobQueueService;
    private final ObjectMapper objectMapper;

    public JobController(JobQueueService jobQueueService, ObjectMapper objectMapper) {
        this.jobQueueService = jobQueueService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/cv-optimization")
    @Operation(summary = "Enqueue a CV optimization background job with idempotency guarantee")
    public ResponseEntity<ApiResponse<JobStatusResponse>> enqueueOptimization(
            @AuthenticationPrincipal Object principal,
            @Valid @RequestBody EnqueueOptimizationRequest request
    ) throws Exception {
        UUID userId = resolveUserId(principal);

        String payloadJson = objectMapper.writeValueAsString(Map.of(
                "cvDocumentId", request.cvDocumentId(),
                "jobDescriptionId", request.jobDescriptionId()
        ));

        ProcessingJobEntity job = jobQueueService.enqueue(
                userId,
                "CV_OPTIMIZATION",
                payloadJson,
                request.idempotencyKey(),
                1 // standard priority
        );

        JobStatusResponse response = toResponse(job);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.ok(response));
    }

    @GetMapping("/{jobId}")
    @Operation(summary = "Get current status and results of a background job")
    public ResponseEntity<ApiResponse<JobStatusResponse>> getJobStatus(
            @AuthenticationPrincipal Object principal,
            @PathVariable UUID jobId
    ) {
        UUID userId = resolveUserId(principal);
        return jobQueueService.getJob(jobId, userId)
                .map(job -> ResponseEntity.ok(ApiResponse.ok(toResponse(job))))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("JOB_NOT_FOUND", "Job not found")));
    }

    @GetMapping("/idempotency/{key}")
    @Operation(summary = "Retrieve job status by client idempotency key")
    public ResponseEntity<ApiResponse<JobStatusResponse>> getJobByIdempotencyKey(
            @AuthenticationPrincipal Object principal,
            @PathVariable String key
    ) {
        UUID userId = resolveUserId(principal);
        return jobQueueService.getJobByIdempotencyKey(key, userId)
                .map(job -> ResponseEntity.ok(ApiResponse.ok(toResponse(job))))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("JOB_NOT_FOUND", "Job not found for idempotency key")));
    }

    private JobStatusResponse toResponse(ProcessingJobEntity job) {
        return new JobStatusResponse(
                job.getId(),
                job.getJobType(),
                job.getStatus(),
                job.getAttemptCount(),
                job.getMaxRetries(),
                job.getIdempotencyKey(),
                job.getResultJson(),
                job.getErrorMessage(),
                job.getCreatedAt(),
                job.getUpdatedAt()
        );
    }

    private UUID resolveUserId(Object principal) {
        if (principal instanceof AuthenticatedUser user) {
            return user.userId();
        } else if (principal instanceof Jwt jwt) {
            String sub = jwt.getSubject();
            if (sub != null && !sub.isBlank()) {
                try {
                    return UUID.fromString(sub);
                } catch (IllegalArgumentException e) {
                    return UUID.nameUUIDFromBytes(sub.getBytes());
                }
            }
        }
        return UUID.fromString("00000000-0000-0000-0000-000000000001");
    }
}
