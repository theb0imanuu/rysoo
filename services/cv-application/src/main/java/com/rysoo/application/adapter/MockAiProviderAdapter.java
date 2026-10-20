package com.rysoo.application.adapter;

import com.rysoo.application.dto.GeneratedClaimsOutput;
import com.rysoo.application.port.out.AiProviderPort;
import com.rysoo.domain.model.CanonicalProfile;
import com.rysoo.domain.model.ConfidenceLevel;
import com.rysoo.domain.model.JobAnalysisResult;
import com.rysoo.domain.model.RequirementEvidence;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Deterministic AI adapter for local development, automated testing, and CI pipelines.
 */
@Component
public class MockAiProviderAdapter implements AiProviderPort {

    private boolean simulateFabrication = false;

    public MockAiProviderAdapter() {
    }

    public MockAiProviderAdapter(boolean simulateFabrication) {
        this.simulateFabrication = simulateFabrication;
    }

    public void setSimulateFabrication(boolean simulateFabrication) {
        this.simulateFabrication = simulateFabrication;
    }

    public boolean isSimulateFabrication() {
        return simulateFabrication;
    }

    @Override
    public JobAnalysisResult analyzeJobDescription(String rawJobDescription) {
        if (rawJobDescription == null || rawJobDescription.trim().length() < 20) {
            throw new IllegalArgumentException("Job description must contain at least 20 characters of substantive text.");
        }

        String roleTitle = "Senior Software Engineer";
        String seniority = "Senior";
        Set<String> requiredSkills = new LinkedHashSet<>();
        Set<String> preferredSkills = new LinkedHashSet<>();
        List<String> responsibilities = new ArrayList<>();
        List<String> domains = new ArrayList<>(List.of("Distributed Systems", "Cloud Infrastructure"));
        List<String> culturalThemes = new ArrayList<>(List.of("Engineering Excellence", "Cross-functional Collaboration"));

        String[] lines = rawJobDescription.split("\n");
        String section = "GENERAL";

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            String lower = trimmed.toLowerCase();
            if (lower.startsWith("position:") || lower.startsWith("title:") || lower.startsWith("role:")) {
                roleTitle = trimmed.substring(trimmed.indexOf(":") + 1).trim();
                if (roleTitle.toLowerCase().contains("senior") || roleTitle.toLowerCase().contains("lead")) {
                    seniority = "Senior";
                } else if (roleTitle.toLowerCase().contains("junior") || roleTitle.toLowerCase().contains("associate")) {
                    seniority = "Junior";
                }
            } else if (lower.contains("responsibilities") || lower.contains("what you will do")) {
                section = "RESPONSIBILITIES";
            } else if (lower.contains("requirements") || lower.contains("qualifications") || lower.contains("what we need")) {
                section = "REQUIREMENTS";
            } else if (lower.contains("nice to have") || lower.contains("preferred") || lower.contains("bonus")) {
                section = "PREFERRED";
            } else {
                if (trimmed.startsWith("-") || trimmed.startsWith("*") || trimmed.matches("^\\d+\\..*")) {
                    String item = trimmed.replaceFirst("^[-*\\d.]+\\s*", "").trim();
                    switch (section) {
                        case "RESPONSIBILITIES" -> responsibilities.add(item);
                        case "REQUIREMENTS" -> {
                            extractSkillsFromSentence(item, requiredSkills);
                        }
                        case "PREFERRED" -> {
                            extractSkillsFromSentence(item, preferredSkills);
                        }
                    }
                }
            }
        }

        // If specific keyword lists found in text, extract directly
        if (rawJobDescription.contains("Rust")) requiredSkills.add("Rust");
        if (rawJobDescription.contains("Solana")) requiredSkills.add("Solana");
        if (rawJobDescription.contains("Ethereum")) requiredSkills.add("Ethereum");
        if (rawJobDescription.contains("Haskell")) requiredSkills.add("Haskell");
        if (rawJobDescription.contains("Java")) requiredSkills.add("Java");
        if (rawJobDescription.contains("Spring Boot")) requiredSkills.add("Spring Boot");
        if (rawJobDescription.contains("PostgreSQL")) requiredSkills.add("PostgreSQL");
        if (rawJobDescription.contains("Docker")) requiredSkills.add("Docker");
        if (rawJobDescription.contains("AWS")) preferredSkills.add("AWS");

        if (requiredSkills.isEmpty()) {
            requiredSkills.addAll(Set.of("Software Engineering", "Problem Solving"));
        }

        return new JobAnalysisResult(
                roleTitle,
                seniority,
                requiredSkills,
                preferredSkills,
                responsibilities,
                domains,
                culturalThemes
        );
    }

    private void extractSkillsFromSentence(String sentence, Set<String> targetSet) {
        String[] keywords = {"Java", "Spring Boot", "PostgreSQL", "Docker", "AWS", "OAuth2", "JWT", "SQL", "JUnit", "Mockito", "Rust", "Solana", "Ethereum", "Haskell", "Kubernetes", "TypeScript", "React"};
        for (String kw : keywords) {
            Pattern p = Pattern.compile("\\b" + Pattern.quote(kw) + "\\b", Pattern.CASE_INSENSITIVE);
            if (p.matcher(sentence).find()) {
                targetSet.add(kw);
            }
        }
    }

    @Override
    public GeneratedClaimsOutput generateTailoredCv(
            CanonicalProfile sourceProfile,
            JobAnalysisResult jobAnalysis,
            List<RequirementEvidence> evidenceMapping
    ) {
        if (simulateFabrication) {
            // Generates hallucinated claims intentionally for safety testing
            return new GeneratedClaimsOutput(
                    "# " + sourceProfile.candidateName() + "\nPh.D. in Quantum Cryptography from MIT (2020)\n10 years of Rust and Solana smart contract engineering...",
                    List.of("CloudScale Inc.", "FinTech Innovations", "Solana Foundation (Fabricated)"),
                    List.of("Bachelor of Science in Computer Science - UT Austin", "Ph.D. in Quantum Cryptography - MIT (Fabricated)"),
                    List.of("Java", "Spring Boot", "Rust (Fabricated)", "Solana (Fabricated)"),
                    List.of("Engineered quantum consensus mechanism securing $50B in assets"),
                    List.of("Emphasized distributed blockchain protocols and quantum cryptography")
            );
        }

        // Clean, verified generation strictly adhering to sourceProfile facts
        List<String> verifiedEmployers = new ArrayList<>(sourceProfile.employers());
        List<String> verifiedDegrees = new ArrayList<>(sourceProfile.degrees());
        
        // Select demonstrated skills matching the job description
        Set<String> demonstratedSkills = evidenceMapping.stream()
                .filter(e -> e.confidenceLevel() == ConfidenceLevel.DEMONSTRATED || e.confidenceLevel() == ConfidenceLevel.PARTIALLY_SUPPORTED)
                .map(RequirementEvidence::requirementText)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // Supplement with candidate's actual skills
        demonstratedSkills.addAll(sourceProfile.skills());

        StringBuilder md = new StringBuilder();
        md.append("# ").append(sourceProfile.candidateName()).append("\n\n");
        md.append(sourceProfile.email()).append(" | ").append(sourceProfile.phone()).append(" | ").append(sourceProfile.location()).append("\n\n");
        md.append("## Professional Summary\n\n");
        md.append("Accomplished Senior Software Engineer tailored for ").append(jobAnalysis.roleTitle())
          .append(". Proven background in ").append(String.join(", ", jobAnalysis.requiredSkills()))
          .append(" with demonstrable success at ").append(String.join(" and ", verifiedEmployers)).append(".\n\n");

        md.append("## Technical Skills\n\n");
        md.append("- **Core Competencies**: ").append(String.join(", ", demonstratedSkills)).append("\n\n");

        md.append("## Experience\n\n");
        if (sourceProfile.workExperiences() != null) {
            for (CanonicalProfile.WorkExperience exp : sourceProfile.workExperiences()) {
                md.append("### ").append(exp.roleTitle()).append(" | ").append(exp.employer()).append("\n");
                md.append("*").append(exp.startDate()).append(" - ").append(exp.endDate()).append("*\n\n");
                if (exp.highlights() != null) {
                    for (String hl : exp.highlights()) {
                        md.append("- ").append(hl).append("\n");
                    }
                }
                md.append("\n");
            }
        }

        md.append("## Education\n\n");
        for (String deg : verifiedDegrees) {
            md.append("- ").append(deg).append("\n");
        }

        List<String> claimedAccomplishments = new ArrayList<>();
        if (sourceProfile.workExperiences() != null) {
            for (CanonicalProfile.WorkExperience exp : sourceProfile.workExperiences()) {
                if (exp.highlights() != null) {
                    claimedAccomplishments.addAll(exp.highlights());
                }
            }
        }

        List<String> improvements = List.of(
                "Restructured professional summary to directly address " + jobAnalysis.roleTitle() + " requirements",
                "Highlighted demonstrated proficiencies in " + String.join(", ", jobAnalysis.requiredSkills()),
                "Emphasized quantified achievements and containerized backend delivery"
        );

        return new GeneratedClaimsOutput(
                md.toString(),
                verifiedEmployers,
                verifiedDegrees,
                new ArrayList<>(demonstratedSkills),
                claimedAccomplishments,
                improvements
        );
    }
}
