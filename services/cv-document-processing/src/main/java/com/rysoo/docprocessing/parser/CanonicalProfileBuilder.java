package com.rysoo.docprocessing.parser;

import com.rysoo.domain.model.CanonicalProfile;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CanonicalProfileBuilder {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    private static final Pattern PHONE_PATTERN = Pattern.compile("(\\+?\\d{1,3}[-.\\s]?)?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}");

    public CanonicalProfile build(String normalizedMarkdown) {
        if (normalizedMarkdown == null || normalizedMarkdown.isBlank()) {
            return CanonicalProfile.empty();
        }

        String[] lines = normalizedMarkdown.split("\n");

        String candidateName = "Unknown";
        String email = "";
        String phone = "";
        String location = "";
        StringBuilder summary = new StringBuilder();

        Set<String> employers = new HashSet<>();
        Set<String> degrees = new HashSet<>();
        Set<String> skills = new HashSet<>();
        List<CanonicalProfile.WorkExperience> experiences = new ArrayList<>();

        String currentSection = "HEADER";
        String currentEmployer = null;
        String currentRole = null;
        String currentDates = null;
        List<String> currentHighlights = new ArrayList<>();

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            // Extract email & phone anywhere in header
            Matcher emailMatcher = EMAIL_PATTERN.matcher(trimmed);
            if (emailMatcher.find() && email.isEmpty()) {
                email = emailMatcher.group();
            }
            Matcher phoneMatcher = PHONE_PATTERN.matcher(trimmed);
            if (phoneMatcher.find() && phone.isEmpty()) {
                phone = phoneMatcher.group();
            }

            // Heading detection
            String upperLine = trimmed.toUpperCase();
            if (upperLine.startsWith("# ") && candidateName.equals("Unknown")) {
                candidateName = trimmed.substring(2).trim();
                continue;
            } else if (upperLine.contains("SUMMARY") || upperLine.contains("PROFILE") || upperLine.contains("ABOUT")) {
                currentSection = "SUMMARY";
                continue;
            } else if (upperLine.contains("EXPERIENCE") || upperLine.contains("EMPLOYMENT") || upperLine.contains("WORK HISTORY")) {
                currentSection = "EXPERIENCE";
                continue;
            } else if (upperLine.contains("EDUCATION") || upperLine.contains("ACADEMIC")) {
                currentSection = "EDUCATION";
                continue;
            } else if (upperLine.contains("SKILLS") || upperLine.contains("TECHNOLOGIES") || upperLine.contains("COMPETENCIES")) {
                currentSection = "SKILLS";
                continue;
            }

            switch (currentSection) {
                case "HEADER" -> {
                    if (candidateName.equals("Unknown") && !trimmed.contains("@") && !trimmed.contains("http")) {
                        candidateName = trimmed.replaceAll("^#+\\s*", "");
                    }
                }
                case "SUMMARY" -> summary.append(trimmed).append(" ");
                case "EXPERIENCE" -> {
                    if (trimmed.startsWith("### ") || trimmed.startsWith("**") || (trimmed.contains("|") && !trimmed.startsWith("-"))) {
                        // Potential new role / employer
                        if (currentEmployer != null) {
                            experiences.add(new CanonicalProfile.WorkExperience(
                                    currentEmployer, currentRole, currentDates, "", new ArrayList<>(currentHighlights)
                            ));
                            currentHighlights.clear();
                        }
                        String clean = trimmed.replaceAll("^###+\\s*", "").replace("**", "");
                        if (clean.contains("|")) {
                            String[] parts = clean.split("\\|");
                            currentRole = parts[0].trim();
                            currentEmployer = parts[1].trim();
                            employers.add(currentEmployer);
                        } else {
                            currentEmployer = clean;
                            employers.add(clean);
                        }
                    } else if (trimmed.startsWith("- ")) {
                        currentHighlights.add(trimmed.substring(2).trim());
                    } else if (trimmed.startsWith("*") && trimmed.endsWith("*")) {
                        currentDates = trimmed.replace("*", "").trim();
                    }
                }
                case "EDUCATION" -> {
                    if (trimmed.startsWith("### ") || trimmed.startsWith("**") || trimmed.contains("Bachelor") || trimmed.contains("Master") || trimmed.contains("Degree")) {
                        String degree = trimmed.replaceAll("^###+\\s*", "").replace("**", "");
                        degrees.add(degree);
                    } else if (trimmed.contains("University") || trimmed.contains("College") || trimmed.contains("Institute")) {
                        degrees.add(trimmed.replace("*", "").trim());
                    }
                }
                case "SKILLS" -> {
                    String cleanSkills = trimmed.replaceAll("^-\\s*\\*\\*[^*]+\\*\\*:\\s*", "").replaceAll("^-\\s*", "");
                    String[] tokens = cleanSkills.split("[,;•|]");
                    for (String token : tokens) {
                        String s = token.trim();
                        if (!s.isEmpty()) {
                            skills.add(s);
                        }
                    }
                }
            }
        }

        if (currentEmployer != null) {
            experiences.add(new CanonicalProfile.WorkExperience(
                    currentEmployer, currentRole, currentDates, "", currentHighlights
            ));
        }

        return new CanonicalProfile(
                candidateName,
                email,
                phone,
                location,
                summary.toString().trim(),
                Collections.unmodifiableSet(employers),
                Collections.unmodifiableSet(degrees),
                Collections.unmodifiableSet(skills),
                Collections.unmodifiableList(experiences)
        );
    }
}
