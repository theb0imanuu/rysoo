package com.rysoo.docprocessing.normalizer;

import java.util.regex.Pattern;

public class MarkdownNormalizer {

    private static final Pattern BULLET_PATTERN = Pattern.compile("^[ \\t]*[•\\*\\–\\—\\▪\\▫\\-]\\s*", Pattern.MULTILINE);
    private static final Pattern EXCESS_NEWLINES = Pattern.compile("\\n{3,}");
    private static final Pattern TRAILING_SPACES = Pattern.compile("[ \\t]+$", Pattern.MULTILINE);

    /**
     * Normalizes raw extracted text into clean, structured Markdown.
     */
    public String normalize(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return "";
        }

        String normalized = rawText.replace("\r\n", "\n").replace("\r", "\n");

        // Standardize bullet points to "- "
        normalized = BULLET_PATTERN.matcher(normalized).replaceAll("- ");

        // Remove trailing spaces per line
        normalized = TRAILING_SPACES.matcher(normalized).replaceAll("");

        // Collapse excess blank lines (max 2 consecutive newlines)
        normalized = EXCESS_NEWLINES.matcher(normalized).replaceAll("\n\n");

        return normalized.trim();
    }
}
