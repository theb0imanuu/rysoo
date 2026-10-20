package com.rysoo.application.port.out;

import com.rysoo.application.dto.GeneratedClaimsOutput;
import com.rysoo.domain.model.CanonicalProfile;
import com.rysoo.domain.model.JobAnalysisResult;
import com.rysoo.domain.model.RequirementEvidence;

import java.util.List;

/**
 * Provider-agnostic outbound port for AI generation and analysis.
 */
public interface AiProviderPort {

    /**
     * Performs structured role and requirements analysis of a job description.
     */
    JobAnalysisResult analyzeJobDescription(String rawJobDescription);

    /**
     * Generates a tailored CV in Markdown adhering strictly to substantiated facts.
     */
    GeneratedClaimsOutput generateTailoredCv(
        CanonicalProfile sourceProfile,
        JobAnalysisResult jobAnalysis,
        List<RequirementEvidence> evidenceMapping
    );
}
