package com.rysoo.application;

import com.rysoo.application.service.EvidenceMappingService;
import com.rysoo.domain.model.CanonicalProfile;
import com.rysoo.domain.model.ConfidenceLevel;
import com.rysoo.domain.model.JobAnalysisResult;
import com.rysoo.domain.model.RequirementEvidence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class EvidenceMappingServiceTest {

    private EvidenceMappingService evidenceMappingService;
    private CanonicalProfile standardCandidate;

    @BeforeEach
    void setUp() {
        evidenceMappingService = new EvidenceMappingService();

        standardCandidate = new CanonicalProfile(
                "Jane Doe",
                "jane.doe.synthetic@example.com",
                "555-0199",
                "San Francisco, CA",
                "Experienced Senior Software Engineer with 7+ years in Java and Spring Boot.",
                Set.of("CloudScale Inc.", "FinTech Innovations"),
                Set.of("Bachelor of Science in Computer Science - University of Texas at Austin"),
                Set.of("Java 17/21", "Spring Boot", "PostgreSQL", "Docker", "AWS", "JUnit 5", "Mockito"),
                List.of(
                        new CanonicalProfile.WorkExperience(
                                "CloudScale Inc.",
                                "Senior Software Engineer",
                                "March 2021",
                                "Present",
                                List.of(
                                        "Architected and deployed high-throughput Spring Boot microservices handling over 15,000 requests per minute.",
                                        "Optimized PostgreSQL queries and connection pooling using HikariCP.",
                                        "Integrated Flyway database migrations and automated CI/CD pipeline deployments using Docker and GitHub Actions."
                                )
                        )
                )
        );
    }

    @Test
    @DisplayName("Should categorize verified candidate skills as DEMONSTRATED with supporting quotes")
    void testDemonstratedEvidenceMapping() {
        JobAnalysisResult standardJd = new JobAnalysisResult(
                "Senior Backend Engineer",
                "Senior",
                Set.of("Java", "Spring Boot", "PostgreSQL", "Docker"),
                Set.of("AWS"),
                List.of("Build microservices", "Optimize databases"),
                List.of("Distributed Systems"),
                List.of("Engineering Excellence")
        );

        List<RequirementEvidence> evidences = evidenceMappingService.mapEvidence(standardCandidate, standardJd);

        Map<String, RequirementEvidence> evidenceMap = evidences.stream()
                .collect(Collectors.toMap(RequirementEvidence::requirementText, e -> e));

        // Java
        assertThat(evidenceMap.get("Java").confidenceLevel()).isEqualTo(ConfidenceLevel.DEMONSTRATED);
        assertThat(evidenceMap.get("Java").matchScore()).isGreaterThanOrEqualTo(0.9);
        assertThat(evidenceMap.get("Java").substantiatedQuote()).isNotBlank();

        // Spring Boot
        assertThat(evidenceMap.get("Spring Boot").confidenceLevel()).isEqualTo(ConfidenceLevel.DEMONSTRATED);
        assertThat(evidenceMap.get("Spring Boot").substantiatedQuote()).contains("Spring Boot microservices");

        // PostgreSQL
        assertThat(evidenceMap.get("PostgreSQL").confidenceLevel()).isEqualTo(ConfidenceLevel.DEMONSTRATED);
        assertThat(evidenceMap.get("PostgreSQL").substantiatedQuote()).contains("PostgreSQL");

        // Docker
        assertThat(evidenceMap.get("Docker").confidenceLevel()).isEqualTo(ConfidenceLevel.DEMONSTRATED);
        assertThat(evidenceMap.get("Docker").substantiatedQuote()).contains("Docker");
    }

    @Test
    @DisplayName("Should categorize unsupported skills as MISSING with null quotes")
    void testUnsupportedSkillsAsMissing() {
        JobAnalysisResult web3Jd = new JobAnalysisResult(
                "Lead Rust & Blockchain Core Engineer",
                "Lead",
                Set.of("Rust", "Solana", "Haskell"),
                Set.of("Post-Quantum Cryptography"),
                List.of("Core consensus protocol"),
                List.of("Blockchain"),
                List.of("Research")
        );

        List<RequirementEvidence> evidences = evidenceMappingService.mapEvidence(standardCandidate, web3Jd);

        Map<String, RequirementEvidence> evidenceMap = evidences.stream()
                .collect(Collectors.toMap(RequirementEvidence::requirementText, e -> e));

        // Rust is not in candidate profile
        assertThat(evidenceMap.get("Rust").confidenceLevel()).isEqualTo(ConfidenceLevel.MISSING);
        assertThat(evidenceMap.get("Rust").matchScore()).isEqualTo(0.0);
        assertThat(evidenceMap.get("Rust").substantiatedQuote()).isNull();

        // Solana is not in candidate profile
        assertThat(evidenceMap.get("Solana").confidenceLevel()).isEqualTo(ConfidenceLevel.MISSING);
        assertThat(evidenceMap.get("Solana").matchScore()).isEqualTo(0.0);
        assertThat(evidenceMap.get("Solana").substantiatedQuote()).isNull();

        // Haskell is not in candidate profile
        assertThat(evidenceMap.get("Haskell").confidenceLevel()).isEqualTo(ConfidenceLevel.MISSING);
        assertThat(evidenceMap.get("Haskell").matchScore()).isEqualTo(0.0);
        assertThat(evidenceMap.get("Haskell").substantiatedQuote()).isNull();
    }
}
