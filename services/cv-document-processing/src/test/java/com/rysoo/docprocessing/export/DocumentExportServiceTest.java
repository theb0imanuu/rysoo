package com.rysoo.docprocessing.export;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentExportServiceTest {

    private DocumentExportService exportService;

    @BeforeEach
    void setUp() {
        exportService = new DocumentExportService();
    }

    @Test
    void exportToPdf_generatesValidPdfBytes() throws IOException {
        String markdown = "# Jane Doe\nSenior Staff Engineer\n\n## Experience\n- Acme Corp (2020-Present)\n- Built Java microservices";
        byte[] pdfBytes = exportService.exportToPdf("Jane Doe", markdown, true, 94);

        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(500);

        // Verify PDF Magic Bytes: %PDF
        String header = new String(pdfBytes, 0, 4, StandardCharsets.US_ASCII);
        assertThat(header).isEqualTo("%PDF");
    }

    @Test
    void exportToDocx_generatesValidWordBytes() throws IOException {
        String markdown = "# Jane Doe\n\n## Experience\n- Acme Corp\n- Built scalable infrastructure";
        byte[] docxBytes = exportService.exportToDocx("Jane Doe", markdown, true, 92);

        assertThat(docxBytes).isNotNull();
        assertThat(docxBytes.length).isGreaterThan(500);

        // Verify ZIP / OOXML Magic Bytes: PK (0x50 0x4B)
        assertThat(docxBytes[0]).isEqualTo((byte) 0x50);
        assertThat(docxBytes[1]).isEqualTo((byte) 0x4B);
    }

    @Test
    void exportToMarkdown_returnsUtf8Bytes() {
        String markdown = "# Alex Chen\nPrincipal Engineer";
        byte[] mdBytes = exportService.exportToMarkdown(markdown);

        assertThat(mdBytes).isNotNull();
        assertThat(new String(mdBytes, StandardCharsets.UTF_8)).isEqualTo(markdown);
    }

    @Test
    void exportToJsonCertificate_containsExpectedFieldsAndHash() {
        String json = exportService.exportToJsonCertificate(
                "Jane Doe",
                "Cloud Architect",
                95,
                94,
                true,
                List.of("Acme Corp", "DataScale"),
                List.of("M.S. Computer Science"),
                List.of("Java", "PostgreSQL", "Docker")
        );

        assertThat(json).contains("\"platform\": \"Rysoo Anti-Hallucination ATS Platform\"");
        assertThat(json).contains("\"candidateName\": \"Jane Doe\"");
        assertThat(json).contains("\"atsScore\": 95");
        assertThat(json).contains("\"sha256VerificationHash\":");
        assertThat(json).contains("\"verifiedEmployers\": [\"Acme Corp\", \"DataScale\"]");
        assertThat(json).contains("\"factuallyCertified\": true");
    }
}
