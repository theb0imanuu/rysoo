package com.rysoo.domain.model;

import java.util.Collections;
import java.util.List;

/**
 * Result of the CV optimization pipeline, including tailored markdown,
 * ATS scoring, keyword coverage, and anti-hallucination verification.
 */
public record OptimizationOutput(
    String tailoredMarkdown,
    List<String> claimedEmployers,
    List<String> claimedDegrees,
    List<String> claimedSkills,
    List<String> claimedAccomplishments,
    int atsScore,
    double keywordCoverageScore,
    FactualCheckResult factualCheckResult,
    List<RequirementEvidence> evidenceMapping,
    List<String> structuralImprovements
) {
    public OptimizationOutput {
        claimedEmployers = (claimedEmployers != null) ? Collections.unmodifiableList(claimedEmployers) : List.of();
        claimedDegrees = (claimedDegrees != null) ? Collections.unmodifiableList(claimedDegrees) : List.of();
        claimedSkills = (claimedSkills != null) ? Collections.unmodifiableList(claimedSkills) : List.of();
        claimedAccomplishments = (claimedAccomplishments != null) ? Collections.unmodifiableList(claimedAccomplishments) : List.of();
        evidenceMapping = (evidenceMapping != null) ? Collections.unmodifiableList(evidenceMapping) : List.of();
        structuralImprovements = (structuralImprovements != null) ? Collections.unmodifiableList(structuralImprovements) : List.of();
    }

    public boolean isVerified() {
        return factualCheckResult != null && factualCheckResult.isClean();
    }
}
