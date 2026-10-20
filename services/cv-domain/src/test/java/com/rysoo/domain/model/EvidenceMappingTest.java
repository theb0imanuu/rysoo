package com.rysoo.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EvidenceMappingTest {

    @Test
    @DisplayName("Demonstrated requirement must contain substantiated evidence quote")
    void demonstratedRequirementRequiresEvidence() {
        RequirementEvidence evidence = new RequirementEvidence(
                "5+ years Java experience",
                "7+ years expertise designing backend systems using Java and Spring Boot",
                ConfidenceLevel.DEMONSTRATED,
                0.95
        );

        assertThat(evidence.confidenceLevel()).isEqualTo(ConfidenceLevel.DEMONSTRATED);
        assertThat(evidence.substantiatedQuote()).isNotEmpty();
    }

    @Test
    @DisplayName("Demonstrated requirement without quote throws exception")
    void demonstratedRequirementWithoutQuoteFails() {
        assertThatThrownBy(() -> new RequirementEvidence(
                "5+ years Java experience",
                "",
                ConfidenceLevel.DEMONSTRATED,
                0.95
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("must provide substantiated evidence");
    }

    @Test
    @DisplayName("Missing requirement cannot claim substantiated evidence quote")
    void missingRequirementWithEvidenceFails() {
        assertThatThrownBy(() -> new RequirementEvidence(
                "Quantum Cryptography",
                "Some claim",
                ConfidenceLevel.MISSING,
                0.0
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("cannot have a substantiated evidence quote");
    }

    @Test
    @DisplayName("Missing requirement with null quote succeeds")
    void missingRequirementValid() {
        RequirementEvidence evidence = new RequirementEvidence(
                "Rust development",
                null,
                ConfidenceLevel.MISSING,
                0.0
        );

        assertThat(evidence.confidenceLevel()).isEqualTo(ConfidenceLevel.MISSING);
        assertThat(evidence.substantiatedQuote()).isNull();
    }
}
