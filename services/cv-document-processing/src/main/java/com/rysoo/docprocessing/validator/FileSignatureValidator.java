package com.rysoo.docprocessing.validator;

import org.apache.tika.Tika;

import java.io.InputStream;
import java.util.Set;

public class FileSignatureValidator {

    private final Tika tika = new Tika();

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain",
            "text/markdown",
            "text/x-web-markdown"
    );

    public record ValidationStatus(
            boolean valid,
            String detectedMimeType,
            String errorMessage
    ) {}

    public ValidationStatus validate(InputStream inputStream, String fileName) {
        try {
            String detectedType = tika.detect(inputStream, fileName);
            if (detectedType == null) {
                return new ValidationStatus(false, "unknown", "Could not detect file type");
            }

            if (ALLOWED_MIME_TYPES.contains(detectedType)) {
                return new ValidationStatus(true, detectedType, null);
            }

            return new ValidationStatus(
                    false,
                    detectedType,
                    "Unsupported or dangerous file signature: " + detectedType + ". Allowed: PDF, DOCX, Markdown."
            );
        } catch (Exception e) {
            return new ValidationStatus(false, "error", "Failed to inspect file signature: " + e.getMessage());
        }
    }
}
