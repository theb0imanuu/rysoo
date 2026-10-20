package com.rysoo.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "optimization_results")
public class OptimizationResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "optimization_run_id", nullable = false, unique = true)
    private OptimizationRunEntity optimizationRun;

    @Column(name = "tailored_markdown", nullable = false, columnDefinition = "TEXT")
    private String tailoredMarkdown;

    @Column(name = "evidence_mapping_json", nullable = false, columnDefinition = "TEXT")
    private String evidenceMappingJson;

    @Column(name = "match_score_percentage", nullable = false)
    private int matchScorePercentage;

    @Column(name = "match_explanation_markdown", nullable = false, columnDefinition = "TEXT")
    private String matchExplanationMarkdown;

    @Column(name = "factual_verification_status", nullable = false, length = 32)
    private String factualVerificationStatus;

    @Column(name = "verification_report_json", nullable = false, columnDefinition = "TEXT")
    private String verificationReportJson = "{}";

    @Column(name = "diff_summary_json", nullable = false, columnDefinition = "TEXT")
    private String diffSummaryJson = "{}";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public OptimizationResultEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public OptimizationRunEntity getOptimizationRun() { return optimizationRun; }
    public void setOptimizationRun(OptimizationRunEntity optimizationRun) { this.optimizationRun = optimizationRun; }

    public String getTailoredMarkdown() { return tailoredMarkdown; }
    public void setTailoredMarkdown(String tailoredMarkdown) { this.tailoredMarkdown = tailoredMarkdown; }

    public String getEvidenceMappingJson() { return evidenceMappingJson; }
    public void setEvidenceMappingJson(String evidenceMappingJson) { this.evidenceMappingJson = evidenceMappingJson; }

    public int getMatchScorePercentage() { return matchScorePercentage; }
    public void setMatchScorePercentage(int matchScorePercentage) { this.matchScorePercentage = matchScorePercentage; }

    public String getMatchExplanationMarkdown() { return matchExplanationMarkdown; }
    public void setMatchExplanationMarkdown(String matchExplanationMarkdown) { this.matchExplanationMarkdown = matchExplanationMarkdown; }

    public String getFactualVerificationStatus() { return factualVerificationStatus; }
    public void setFactualVerificationStatus(String factualVerificationStatus) { this.factualVerificationStatus = factualVerificationStatus; }

    public String getVerificationReportJson() { return verificationReportJson; }
    public void setVerificationReportJson(String verificationReportJson) { this.verificationReportJson = verificationReportJson; }

    public String getDiffSummaryJson() { return diffSummaryJson; }
    public void setDiffSummaryJson(String diffSummaryJson) { this.diffSummaryJson = diffSummaryJson; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
