package com.rysoo.domain.service;

import com.rysoo.domain.model.FactCheckViolation;
import com.rysoo.domain.model.FactualCheckResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FactualIntegrityVerifier {

    /**
     * Verifies that candidate claims in the optimized CV are strictly grounded in source CV evidence.
     * Prevents AI hallucinations of employers, degrees, certifications, or skills.
     */
    public FactualCheckResult verify(
            Set<String> sourceEmployers,
            Set<String> sourceDegrees,
            Set<String> sourceSkills,
            List<String> claimedEmployers,
            List<String> claimedDegrees,
            List<String> claimedSkills
    ) {
        List<FactCheckViolation> violations = new ArrayList<>();

        Set<String> normalizedSourceEmployers = normalizeSet(sourceEmployers);
        Set<String> normalizedSourceDegrees = normalizeSet(sourceDegrees);
        Set<String> normalizedSourceSkills = normalizeSet(sourceSkills);

        // Verify Employers
        if (claimedEmployers != null) {
            for (String employer : claimedEmployers) {
                if (!matchesAny(employer, normalizedSourceEmployers)) {
                    violations.add(new FactCheckViolation(
                            "EMPLOYER",
                            employer,
                            "Employer '" + employer + "' is not present in the candidate's verified source CV."
                    ));
                }
            }
        }

        // Verify Degrees / Education
        if (claimedDegrees != null) {
            for (String degree : claimedDegrees) {
                if (!matchesAny(degree, normalizedSourceDegrees)) {
                    violations.add(new FactCheckViolation(
                            "DEGREE",
                            degree,
                            "Degree or institution '" + degree + "' is not supported by source CV evidence."
                    ));
                }
            }
        }

        // Verify Skills
        if (claimedSkills != null) {
            for (String skill : claimedSkills) {
                if (!matchesAny(skill, normalizedSourceSkills)) {
                    violations.add(new FactCheckViolation(
                            "SKILL",
                            skill,
                            "Skill '" + skill + "' cannot be claimed without source evidence."
                    ));
                }
            }
        }

        if (violations.isEmpty()) {
            return FactualCheckResult.success();
        } else {
            return FactualCheckResult.failure(violations);
        }
    }

    private Set<String> normalizeSet(Set<String> items) {
        if (items == null) {
            return Set.of();
        }
        return items.stream()
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    private boolean matchesAny(String claim, Set<String> sourceSet) {
        if (claim == null || claim.isBlank()) {
            return true;
        }
        String normalizedClaim = claim.trim().toLowerCase();
        for (String source : sourceSet) {
            if (normalizedClaim.contains(source) || source.contains(normalizedClaim)) {
                return true;
            }
        }
        return false;
    }
}
