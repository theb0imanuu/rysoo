package com.rysoo.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ExportDocumentRequest(
        @NotBlank(message = "Candidate name is required")
        String candidateName,

        @NotBlank(message = "Markdown content is required")
        String markdown,

        @NotNull(message = "Export format is required (PDF, DOCX, MARKDOWN, JSON)")
        String format,

        int atsScore,

        boolean factuallyCertified,

        String targetRole,

        int keywordCoverageScore,

        List<String> verifiedEmployers,

        List<String> verifiedDegrees,

        List<String> verifiedSkills
) {
}
