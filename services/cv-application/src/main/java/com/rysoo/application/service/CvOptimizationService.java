package com.rysoo.application.service;

import com.rysoo.application.dto.GeneratedClaimsOutput;
import com.rysoo.application.port.out.AiProviderPort;
import com.rysoo.domain.model.*;
import com.rysoo.domain.service.FactualIntegrityVerifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class CvOptimizationService {

    private final JobAnalysisService jobAnalysisService;
    private final EvidenceMappingService evidenceMappingService;
    private final AiProviderPort aiProviderPort;
    private final FactualIntegrityVerifier factualIntegrityVerifier;

    public CvOptimizationService(
            JobAnalysisService jobAnalysisService,
            EvidenceMappingService evidenceMappingService,
            AiProviderPort aiProviderPort
    ) {
        this.jobAnalysisService = Objects.requireNonNull(jobAnalysisService, "jobAnalysisService must not be null");
        this.evidenceMappingService = Objects.requireNonNull(evidenceMappingService, "evidenceMappingService must not be null");
        this.aiProviderPort = Objects.requireNonNull(aiProviderPort, "aiProviderPort must not be null");
        this.factualIntegrityVerifier = new FactualIntegrityVerifier();
    }

    public OptimizationOutput optimize(CanonicalProfile sourceProfile, String rawJobDescription) {
        Objects.requireNonNull(sourceProfile, "sourceProfile must not be null");

        // 1. Job Analysis
        JobAnalysisResult jobAnalysis = jobAnalysisService.analyze(rawJobDescription);

        // 2. Evidence Mapping
        List<RequirementEvidence> evidenceMapping = evidenceMappingService.mapEvidence(sourceProfile, jobAnalysis);

        // 3. AI Tailoring Generation
        GeneratedClaimsOutput generated = aiProviderPort.generateTailoredCv(sourceProfile, jobAnalysis, evidenceMapping);

        // 4. Strict Factual Integrity Audit (Zero Tolerance for Hallucinations)
        FactualCheckResult checkResult = factualIntegrityVerifier.verify(
                sourceProfile.employers(),
                sourceProfile.degrees(),
                sourceProfile.skills(),
                generated.claimedEmployers(),
                generated.claimedDegrees(),
                generated.claimedSkills()
        );

        // 5. Calculate ATS Score & Keyword Coverage
        int atsScore = calculateAtsScore(jobAnalysis, evidenceMapping);
        double keywordCoverage = calculateKeywordCoverage(jobAnalysis, evidenceMapping);

        return new OptimizationOutput(
                generated.tailoredMarkdown(),
                generated.claimedEmployers(),
                generated.claimedDegrees(),
                generated.claimedSkills(),
                generated.claimedAccomplishments(),
                atsScore,
                keywordCoverage,
                checkResult,
                evidenceMapping,
                generated.structuralImprovements()
        );
    }

    private int calculateAtsScore(JobAnalysisResult jobAnalysis, List<RequirementEvidence> evidence) {
        if (evidence == null || evidence.isEmpty()) {
            return 0;
        }

        double totalWeight = 0.0;
        double earnedWeight = 0.0;

        Set<String> required = jobAnalysis.requiredSkills();
        for (RequirementEvidence req : evidence) {
            double weight = required.contains(req.requirementText()) ? 2.0 : 1.0;
            totalWeight += weight;
            earnedWeight += weight * req.matchScore();
        }

        if (totalWeight == 0) return 100;
        return (int) Math.round((earnedWeight / totalWeight) * 100.0);
    }

    private double calculateKeywordCoverage(JobAnalysisResult jobAnalysis, List<RequirementEvidence> evidence) {
        Set<String> required = jobAnalysis.requiredSkills();
        if (required.isEmpty()) return 100.0;

        long demonstratedCount = evidence.stream()
                .filter(e -> required.contains(e.requirementText()))
                .filter(e -> e.confidenceLevel() == ConfidenceLevel.DEMONSTRATED || e.confidenceLevel() == ConfidenceLevel.PARTIALLY_SUPPORTED)
                .count();

        return Math.round(((double) demonstratedCount / required.size()) * 1000.0) / 10.0;
    }
}
