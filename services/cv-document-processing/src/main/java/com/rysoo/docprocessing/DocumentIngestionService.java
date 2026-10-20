package com.rysoo.docprocessing;

import com.rysoo.domain.model.CanonicalProfile;
import com.rysoo.docprocessing.model.ExtractionResult;
import com.rysoo.docprocessing.normalizer.MarkdownNormalizer;
import com.rysoo.docprocessing.parser.CanonicalProfileBuilder;
import com.rysoo.docprocessing.validator.FileSignatureValidator;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

public class DocumentIngestionService {

    private static final Logger log = LoggerFactory.getLogger(DocumentIngestionService.class);

    private final FileSignatureValidator signatureValidator = new FileSignatureValidator();
    private final MarkdownNormalizer markdownNormalizer = new MarkdownNormalizer();
    private final CanonicalProfileBuilder profileBuilder = new CanonicalProfileBuilder();
    private final Tika tika = new Tika();

    public ExtractionResult ingest(byte[] fileBytes, String fileName) {
        if (fileBytes == null || fileBytes.length == 0) {
            throw new IllegalArgumentException("File content cannot be empty.");
        }

        // 1. Calculate SHA-256 provenance hash
        String sha256 = calculateSha256(fileBytes);

        // 2. Inspect and validate file signature
        FileSignatureValidator.ValidationStatus status = signatureValidator.validate(
                new ByteArrayInputStream(fileBytes), fileName
        );

        if (!status.valid()) {
            throw new IllegalArgumentException(status.errorMessage());
        }

        List<String> warnings = new ArrayList<>();
        String rawText = "";

        // 3. Extract text
        try (InputStream stream = new ByteArrayInputStream(fileBytes)) {
            if ("text/plain".equals(status.detectedMimeType()) || "text/markdown".equals(status.detectedMimeType())) {
                rawText = new String(fileBytes, StandardCharsets.UTF_8);
            } else {
                rawText = tika.parseToString(stream);
            }
        } catch (Exception e) {
            log.error("Failed to parse document text: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to extract content from document: " + e.getMessage(), e);
        }

        // 4. Check for empty extraction / scanned PDF OCR fallback
        boolean ocrFallback = false;
        if (rawText == null || rawText.trim().isEmpty()) {
            if ("application/pdf".equals(status.detectedMimeType())) {
                ocrFallback = true;
                warnings.add("Document appears to be a scanned image-based PDF. OCR fallback required.");
                rawText = "[Scanned document detected - pending OCR extraction]";
            } else {
                throw new IllegalArgumentException("Document contains no readable text content.");
            }
        }

        // 5. Normalize into Markdown
        String normalizedMarkdown = markdownNormalizer.normalize(rawText);

        // 6. Build canonical fact profile
        CanonicalProfile canonicalProfile = profileBuilder.build(normalizedMarkdown);

        return new ExtractionResult(
                rawText,
                normalizedMarkdown,
                status.detectedMimeType(),
                sha256,
                canonicalProfile,
                ocrFallback,
                warnings
        );
    }

    private String calculateSha256(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(bytes);
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
