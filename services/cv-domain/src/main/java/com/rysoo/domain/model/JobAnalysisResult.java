package com.rysoo.domain.model;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Structured breakdown of a target job description extracted via AI analysis.
 */
public record JobAnalysisResult(
    String roleTitle,
    String seniorityLevel,
    Set<String> requiredSkills,
    Set<String> preferredSkills,
    List<String> coreResponsibilities,
    List<String> keyDomains,
    List<String> culturalThemes
) {
    public JobAnalysisResult {
        requiredSkills = (requiredSkills != null) ? Collections.unmodifiableSet(requiredSkills) : Set.of();
        preferredSkills = (preferredSkills != null) ? Collections.unmodifiableSet(preferredSkills) : Set.of();
        coreResponsibilities = (coreResponsibilities != null) ? Collections.unmodifiableList(coreResponsibilities) : List.of();
        keyDomains = (keyDomains != null) ? Collections.unmodifiableList(keyDomains) : List.of();
        culturalThemes = (culturalThemes != null) ? Collections.unmodifiableList(culturalThemes) : List.of();
    }
}
