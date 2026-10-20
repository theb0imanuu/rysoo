package com.rysoo.docprocessing.model;

import com.rysoo.domain.model.CanonicalProfile;
import java.util.List;

public record ExtractionResult(
    String rawText,
    String normalizedMarkdown,
    String detectedMimeType,
    String contentHashSha256,
    CanonicalProfile canonicalProfile,
    boolean ocrFallbackRequired,
    List<String> warnings
) {}
