package com.rysoo.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "optimization_runs")
public class OptimizationRunEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private OptimizationProjectEntity project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_cv_version_id", nullable = false)
    private CvVersionEntity sourceCvVersion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_description_id", nullable = false)
    private JobDescriptionEntity jobDescription;

    @Column(name = "ai_provider", nullable = false, length = 64)
    private String aiProvider;

    @Column(name = "ai_model_identifier", nullable = false, length = 128)
    private String aiModelIdentifier;

    @Column(name = "prompt_version", nullable = false, length = 32)
    private String promptVersion;

    @Column(nullable = false, length = 32)
    private String status = "QUEUED";

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount = 0;

    @Column(name = "token_usage_input", nullable = false)
    private int tokenUsageInput = 0;

    @Column(name = "token_usage_output", nullable = false)
    private int tokenUsageOutput = 0;

    @Column(name = "estimated_cost_usd", nullable = false, precision = 10, scale = 6)
    private BigDecimal estimatedCostUsd = BigDecimal.ZERO;

    @Column(name = "error_code", length = 64)
    private String errorCode;

    @Column(name = "sanitized_error_message", columnDefinition = "TEXT")
    private String sanitizedErrorMessage;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @OneToOne(mappedBy = "optimizationRun", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private OptimizationResultEntity result;

    public OptimizationRunEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public OptimizationProjectEntity getProject() { return project; }
    public void setProject(OptimizationProjectEntity project) { this.project = project; }

    public CvVersionEntity getSourceCvVersion() { return sourceCvVersion; }
    public void setSourceCvVersion(CvVersionEntity sourceCvVersion) { this.sourceCvVersion = sourceCvVersion; }

    public JobDescriptionEntity getJobDescription() { return jobDescription; }
    public void setJobDescription(JobDescriptionEntity jobDescription) { this.jobDescription = jobDescription; }

    public String getAiProvider() { return aiProvider; }
    public void setAiProvider(String aiProvider) { this.aiProvider = aiProvider; }

    public String getAiModelIdentifier() { return aiModelIdentifier; }
    public void setAiModelIdentifier(String aiModelIdentifier) { this.aiModelIdentifier = aiModelIdentifier; }

    public String getPromptVersion() { return promptVersion; }
    public void setPromptVersion(String promptVersion) { this.promptVersion = promptVersion; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getAttemptCount() { return attemptCount; }
    public void setAttemptCount(int attemptCount) { this.attemptCount = attemptCount; }

    public int getTokenUsageInput() { return tokenUsageInput; }
    public void setTokenUsageInput(int tokenUsageInput) { this.tokenUsageInput = tokenUsageInput; }

    public int getTokenUsageOutput() { return tokenUsageOutput; }
    public void setTokenUsageOutput(int tokenUsageOutput) { this.tokenUsageOutput = tokenUsageOutput; }

    public BigDecimal getEstimatedCostUsd() { return estimatedCostUsd; }
    public void setEstimatedCostUsd(BigDecimal estimatedCostUsd) { this.estimatedCostUsd = estimatedCostUsd; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getSanitizedErrorMessage() { return sanitizedErrorMessage; }
    public void setSanitizedErrorMessage(String sanitizedErrorMessage) { this.sanitizedErrorMessage = sanitizedErrorMessage; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public OptimizationResultEntity getResult() { return result; }
    public void setResult(OptimizationResultEntity result) {
        this.result = result;
        if (result != null) {
            result.setOptimizationRun(this);
        }
    }
}
