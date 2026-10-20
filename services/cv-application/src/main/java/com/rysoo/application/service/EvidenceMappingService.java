package com.rysoo.application.service;

import com.rysoo.domain.model.CanonicalProfile;
import com.rysoo.domain.model.ConfidenceLevel;
import com.rysoo.domain.model.JobAnalysisResult;
import com.rysoo.domain.model.RequirementEvidence;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EvidenceMappingService {

    public List<RequirementEvidence> mapEvidence(CanonicalProfile profile, JobAnalysisResult jobAnalysis) {
        Objects.requireNonNull(profile, "profile must not be null");
        Objects.requireNonNull(jobAnalysis, "jobAnalysis must not be null");

        List<RequirementEvidence> mappings = new ArrayList<>();
        Set<String> candidateSkills = normalizeSet(profile.skills());

        // Collect all verifiable bullet points/quotes from work experience
        List<String> highlights = new ArrayList<>();
        if (profile.workExperiences() != null) {
            for (CanonicalProfile.WorkExperience exp : profile.workExperiences()) {
                if (exp.highlights() != null) {
                    highlights.addAll(exp.highlights());
                }
            }
        }

        // 1. Evaluate Required Skills
        for (String reqSkill : jobAnalysis.requiredSkills()) {
            mappings.add(evaluateSkillOrRequirement(reqSkill, candidateSkills, highlights));
        }

        // 2. Evaluate Preferred Skills
        for (String prefSkill : jobAnalysis.preferredSkills()) {
            mappings.add(evaluateSkillOrRequirement(prefSkill, candidateSkills, highlights));
        }

        return Collections.unmodifiableList(mappings);
    }

    private RequirementEvidence evaluateSkillOrRequirement(
            String requirement,
            Set<String> candidateSkills,
            List<String> highlights
    ) {
        String reqLower = requirement.toLowerCase().trim();

        // Direct candidate skill match
        boolean directSkillMatch = candidateSkills.stream()
                .anyMatch(skill -> skill.contains(reqLower) || reqLower.contains(skill));

        // Search for substantiated quote in experience highlights
        String bestQuote = null;
        for (String hl : highlights) {
            if (hl.toLowerCase().contains(reqLower)) {
                bestQuote = hl;
                break;
            }
        }

        if (directSkillMatch && bestQuote != null) {
            return new RequirementEvidence(
                    requirement,
                    bestQuote,
                    ConfidenceLevel.DEMONSTRATED,
                    1.0
            );
        } else if (directSkillMatch) {
            return new RequirementEvidence(
                    requirement,
                    "Direct skill match in candidate's verified profile: " + requirement,
                    ConfidenceLevel.DEMONSTRATED,
                    0.9
            );
        } else if (bestQuote != null) {
            return new RequirementEvidence(
                    requirement,
                    bestQuote,
                    ConfidenceLevel.PARTIALLY_SUPPORTED,
                    0.7
            );
        } else {
            // No evidence found in candidate profile
            return new RequirementEvidence(
                    requirement,
                    null,
                    ConfidenceLevel.MISSING,
                    0.0
            );
        }
    }

    private Set<String> normalizeSet(Set<String> set) {
        if (set == null) return Set.of();
        Set<String> normalized = new HashSet<>();
        for (String s : set) {
            if (s != null) normalized.add(s.trim().toLowerCase());
        }
        return normalized;
    }
}
