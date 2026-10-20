package com.rysoo.domain.model;

import java.util.Objects;

public record RequirementEvidence(
    String requirementText,
    String substantiatedQuote,
    ConfidenceLevel confidenceLevel,
    double matchScore
) {
    public RequirementEvidence {
        Objects.requireNonNull(requirementText, "requirementText must not be null");
        Objects.requireNonNull(confidenceLevel, "confidenceLevel must not be null");
        if (confidenceLevel == ConfidenceLevel.MISSING && substantiatedQuote != null && !substantiatedQuote.isBlank()) {
            throw new IllegalArgumentException("A missing requirement cannot have a substantiated evidence quote");
        }
        if (confidenceLevel == ConfidenceLevel.DEMONSTRATED && (substantiatedQuote == null || substantiatedQuote.isBlank())) {
            throw new IllegalArgumentException("A demonstrated requirement must provide substantiated evidence from the source CV");
        }
    }
}
