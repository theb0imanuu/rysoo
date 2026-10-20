package com.rysoo.application;

import com.rysoo.application.adapter.MockAiProviderAdapter;
import com.rysoo.application.service.CvOptimizationService;
import com.rysoo.application.service.EvidenceMappingService;
import com.rysoo.application.service.JobAnalysisService;
import com.rysoo.domain.model.CanonicalProfile;
import com.rysoo.domain.model.FactCheckViolation;
import com.rysoo.domain.model.OptimizationOutput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CvOptimizationServiceTest {

    private CvOptimizationService cvOptimizationService;
    private MockAiProviderAdapter mockAiAdapter;
    private CanonicalProfile standardCandidate;
    private String standardJobDescription;

    @BeforeEach
    void setUp() throws IOException {
        mockAiAdapter = new MockAiProviderAdapter();
        JobAnalysisService jobAnalysisService = new JobAnalysisService(mockAiAdapter);
        EvidenceMappingService evidenceMappingService = new EvidenceMappingService();

        cvOptimizationService = new CvOptimizationService(
                jobAnalysisService,
                evidenceMappingService,
                mockAiAdapter
        );

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
                                        "Optimized PostgreSQL queries and connection pooling using HikariCP."
                                )
                        )
                )
        );

        standardJobDescription = Files.readString(Path.of("../../tests/fixtures/job-description-standard.txt"));
    }

    @Test
    @DisplayName("Should successfully verify clean optimization without hallucinations")
    void testSuccessfulVerifiedOptimization() {
        mockAiAdapter.setSimulateFabrication(false);

        OptimizationOutput output = cvOptimizationService.optimize(standardCandidate, standardJobDescription);

        assertThat(output).isNotNull();
        assertThat(output.isVerified()).isTrue();
        assertThat(output.factualCheckResult().isClean()).isTrue();
        assertThat(output.factualCheckResult().violations()).isEmpty();
        assertThat(output.atsScore()).isGreaterThanOrEqualTo(75);
        assertThat(output.keywordCoverageScore()).isGreaterThanOrEqualTo(75.0);
        assertThat(output.tailoredMarkdown()).contains("Jane Doe");
        assertThat(output.tailoredMarkdown()).contains("CloudScale Inc.");
        assertThat(output.tailoredMarkdown()).contains("Spring Boot");
    }

    @Test
    @DisplayName("Should catch and flag hallucinated employers, degrees, and skills")
    void testCatchesAndFlagsHallucinatedClaims() {
        mockAiAdapter.setSimulateFabrication(true);

        OptimizationOutput output = cvOptimizationService.optimize(standardCandidate, standardJobDescription);

        assertThat(output).isNotNull();
        assertThat(output.isVerified()).isFalse();
        assertThat(output.factualCheckResult().isClean()).isFalse();

        List<String> violationTypes = output.factualCheckResult().violations().stream()
                .map(FactCheckViolation::category)
                .toList();

        assertThat(violationTypes).contains("EMPLOYER", "DEGREE", "SKILL");

        List<String> claimedValues = output.factualCheckResult().violations().stream()
                .map(FactCheckViolation::claimedFact)
                .toList();

        assertThat(claimedValues).anyMatch(v -> v.contains("Solana Foundation"));
        assertThat(claimedValues).anyMatch(v -> v.contains("Quantum Cryptography"));
        assertThat(claimedValues).anyMatch(v -> v.contains("Rust"));
    }
}
