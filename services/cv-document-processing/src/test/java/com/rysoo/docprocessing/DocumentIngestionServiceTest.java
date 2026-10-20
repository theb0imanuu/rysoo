package com.rysoo.docprocessing;

import com.rysoo.docprocessing.model.ExtractionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentIngestionServiceTest {

    private DocumentIngestionService ingestionService;

    @BeforeEach
    void setUp() {
        ingestionService = new DocumentIngestionService();
    }

    @Test
    @DisplayName("Successfully ingests synthetic Markdown CV and extracts canonical profile")
    void ingestsStandardSyntheticCv() throws IOException {
        Path fixturePath = Path.of("../../tests/fixtures/synthetic-resume-standard.md");
        byte[] bytes = Files.readAllBytes(fixturePath);

        ExtractionResult result = ingestionService.ingest(bytes, "synthetic-resume-standard.md");

        assertThat(result).isNotNull();
        assertThat(result.contentHashSha256()).isNotEmpty();
        assertThat(result.normalizedMarkdown()).contains("Jane Doe");
        assertThat(result.canonicalProfile().candidateName()).isEqualTo("Jane Doe");
        assertThat(result.canonicalProfile().email()).isEqualTo("jane.doe.synthetic@example.com");
        assertThat(result.canonicalProfile().employers()).contains("CloudScale Inc.", "FinTech Innovations");
        assertThat(result.canonicalProfile().degrees()).anyMatch(d -> d.contains("Bachelor of Science in Computer Science"));
    }

    @Test
    @DisplayName("Rejects unsupported executable binaries based on file signature")
    void rejectsUnsupportedFileSignature() throws IOException {
        Path fixturePath = Path.of("../../tests/fixtures/unsupported-file.exe");
        byte[] bytes = Files.readAllBytes(fixturePath);

        assertThatThrownBy(() -> ingestionService.ingest(bytes, "resume.exe"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported or dangerous file signature");
    }

    @Test
    @DisplayName("Rejects malformed corrupt binary files")
    void rejectsMalformedBinaryFile() throws IOException {
        Path fixturePath = Path.of("../../tests/fixtures/malformed-file.bin");
        byte[] bytes = Files.readAllBytes(fixturePath);

        assertThatThrownBy(() -> ingestionService.ingest(bytes, "corrupt.bin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported or dangerous file signature");
    }

    @Test
    @DisplayName("Rejects empty file bytes")
    void rejectsEmptyFile() {
        assertThatThrownBy(() -> ingestionService.ingest(new byte[0], "empty.pdf"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File content cannot be empty");
    }
}
