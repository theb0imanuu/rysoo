package com.rysoo.domain.model;

import java.util.List;
import java.util.Set;

/**
 * Domain representation of candidate ground truth facts extracted from verified source CVs.
 */
public record CanonicalProfile(
    String candidateName,
    String email,
    String phone,
    String location,
    String summary,
    Set<String> employers,
    Set<String> degrees,
    Set<String> skills,
    List<WorkExperience> workExperiences
) {
    public record WorkExperience(
        String employer,
        String roleTitle,
        String startDate,
        String endDate,
        List<String> highlights
    ) {}

    public static CanonicalProfile empty() {
        return new CanonicalProfile(
            "Unknown", "", "", "", "",
            Set.of(), Set.of(), Set.of(), List.of()
        );
    }
}
