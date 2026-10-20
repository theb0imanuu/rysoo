package com.rysoo.application.dto;

import java.util.Collections;
import java.util.List;

public record GeneratedClaimsOutput(
    String tailoredMarkdown,
    List<String> claimedEmployers,
    List<String> claimedDegrees,
    List<String> claimedSkills,
    List<String> claimedAccomplishments,
    List<String> structuralImprovements
) {
    public GeneratedClaimsOutput {
        claimedEmployers = (claimedEmployers != null) ? Collections.unmodifiableList(claimedEmployers) : List.of();
        claimedDegrees = (claimedDegrees != null) ? Collections.unmodifiableList(claimedDegrees) : List.of();
        claimedSkills = (claimedSkills != null) ? Collections.unmodifiableList(claimedSkills) : List.of();
        claimedAccomplishments = (claimedAccomplishments != null) ? Collections.unmodifiableList(claimedAccomplishments) : List.of();
        structuralImprovements = (structuralImprovements != null) ? Collections.unmodifiableList(structuralImprovements) : List.of();
    }
}
