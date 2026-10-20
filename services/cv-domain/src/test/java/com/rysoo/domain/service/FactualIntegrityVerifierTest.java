package com.rysoo.domain.service;

import com.rysoo.domain.model.FactCheckViolation;
import com.rysoo.domain.model.FactualCheckResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FactualIntegrityVerifierTest {

    private FactualIntegrityVerifier verifier;

    private final Set<String> sourceEmployers = Set.of("CloudScale Inc.", "FinTech Innovations");
    private final Set<String> sourceDegrees = Set.of("Bachelor of Science in Computer Science", "University of Texas at Austin");
    private final Set<String> sourceSkills = Set.of("Java", "Spring Boot", "PostgreSQL", "Docker", "AWS", "JUnit 5", "Mockito");

    @BeforeEach
    void setUp() {
        verifier = new FactualIntegrityVerifier();
    }

    @Test
    @DisplayName("Verified claims strictly grounded in CV pass verification")
    void verifiedClaimsPass() {
        List<String> claimedEmployers = List.of("CloudScale Inc.", "FinTech Innovations");
        List<String> claimedDegrees = List.of("Bachelor of Science in Computer Science - University of Texas at Austin");
        List<String> claimedSkills = List.of("Java", "Spring Boot", "PostgreSQL");

        FactualCheckResult result = verifier.verify(
                sourceEmployers,
                sourceDegrees,
                sourceSkills,
                claimedEmployers,
                claimedDegrees,
                claimedSkills
        );

        assertThat(result.passed()).isTrue();
        assertThat(result.violations()).isEmpty();
    }

    @Test
    @DisplayName("Fabricated degrees, employers, or skills are caught and rejected")
    void fabricatedClaimsFail() {
        // AI attempted to invent a PhD from MIT, Solana Foundation employer, and Rust skill
        List<String> claimedEmployers = List.of("CloudScale Inc.", "Solana Foundation");
        List<String> claimedDegrees = List.of("Ph.D. in Quantum Cryptography - MIT");
        List<String> claimedSkills = List.of("Java", "Rust", "Solana");

        FactualCheckResult result = verifier.verify(
                sourceEmployers,
                sourceDegrees,
                sourceSkills,
                claimedEmployers,
                claimedDegrees,
                claimedSkills
        );

        assertThat(result.passed()).isFalse();
        assertThat(result.violations()).hasSize(4);

        Set<String> categories = result.violations().stream()
                .map(FactCheckViolation::category)
                .collect(java.util.stream.Collectors.toSet());

        assertThat(categories).containsExactlyInAnyOrder("EMPLOYER", "DEGREE", "SKILL");
    }
}
