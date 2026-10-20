package com.rysoo.api.controller;

import com.rysoo.api.dto.ExportDocumentRequest;
import com.rysoo.docprocessing.export.DocumentExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/cv/export")
@Tag(name = "CV Export", description = "Endpoints for exporting fact-certified CVs in PDF, DOCX, Markdown, and JSON formats")
public class ExportController {

    private static final Logger log = LoggerFactory.getLogger(ExportController.class);
    private final DocumentExportService exportService = new DocumentExportService();

    @PostMapping
    @Operation(summary = "Export optimized CV document in requested format")
    public ResponseEntity<byte[]> exportCv(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId,
            @Valid @RequestBody ExportDocumentRequest request) throws IOException {

        String userId = jwt != null ? jwt.getSubject() : "anonymous";
        log.info("Export requested by user {} for format {} (Correlation: {})", userId, request.format(), correlationId);

        String format = request.format().toUpperCase();
        String safeName = request.candidateName().replaceAll("\\s+", "_");

        byte[] content;
        String contentType;
        String filename;

        switch (format) {
            case "PDF" -> {
                content = exportService.exportToPdf(
                        request.candidateName(),
                        request.markdown(),
                        request.factuallyCertified(),
                        request.atsScore()
                );
                contentType = "application/pdf";
                filename = safeName + "_Tailored_CV.pdf";
            }
            case "DOCX" -> {
                content = exportService.exportToDocx(
                        request.candidateName(),
                        request.markdown(),
                        request.factuallyCertified(),
                        request.atsScore()
                );
                contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                filename = safeName + "_Tailored_CV.docx";
            }
            case "JSON" -> {
                String json = exportService.exportToJsonCertificate(
                        request.candidateName(),
                        request.targetRole() != null ? request.targetRole() : "Target Role",
                        request.atsScore(),
                        request.keywordCoverageScore(),
                        request.factuallyCertified(),
                        request.verifiedEmployers(),
                        request.verifiedDegrees(),
                        request.verifiedSkills()
                );
                content = json.getBytes(StandardCharsets.UTF_8);
                contentType = "application/json; charset=UTF-8";
                filename = safeName + "_Rysoo_Integrity_Certificate.json";
            }
            case "MARKDOWN" -> {
                content = exportService.exportToMarkdown(request.markdown());
                contentType = "text/markdown; charset=UTF-8";
                filename = safeName + "_Tailored_CV.md";
            }
            default -> throw new IllegalArgumentException("Unsupported export format: " + request.format());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
        if (correlationId != null) {
            headers.set("X-Correlation-ID", correlationId);
        }

        return ResponseEntity.ok()
                .headers(headers)
                .body(content);
    }
}
