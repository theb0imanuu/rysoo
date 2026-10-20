package com.rysoo.docprocessing.export;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;

/**
 * Service for rendering optimized CV deliverables into PDF, Word (DOCX),
 * Markdown, and cryptographic JSON audit certificates.
 */
public class DocumentExportService {

    private static final Logger log = LoggerFactory.getLogger(DocumentExportService.class);

    public byte[] exportToMarkdown(String markdown) {
        if (markdown == null) {
            return new byte[0];
        }
        return markdown.getBytes(StandardCharsets.UTF_8);
    }

    public byte[] exportToPdf(String candidateName, String markdown, boolean factuallyCertified, int atsScore) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                // Fonts
                PDType1Font titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                PDType1Font bodyFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                PDType1Font smallFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

                float margin = 50;
                float y = page.getMediaBox().getHeight() - margin;

                // Title
                stream.beginText();
                stream.setFont(titleFont, 18);
                stream.newLineAtOffset(margin, y);
                String safeName = (candidateName != null && !candidateName.isBlank()) ? candidateName : "Curriculum Vitae";
                stream.showText(sanitizeText(safeName));
                stream.endText();

                y -= 25;

                // Header Badge
                stream.beginText();
                stream.setFont(smallFont, 9);
                stream.newLineAtOffset(margin, y);
                String stamp = String.format("Rysoo ATS Alignment Score: %d%% | Anti-Hallucination: %s",
                        atsScore, factuallyCertified ? "100% Certified Grounded" : "Flagged");
                stream.showText(sanitizeText(stamp));
                stream.endText();

                y -= 20;

                // Horizontal Divider
                stream.setLineWidth(1.0f);
                stream.moveTo(margin, y);
                stream.lineTo(page.getMediaBox().getWidth() - margin, y);
                stream.stroke();

                y -= 20;

                // Body content lines
                stream.beginText();
                stream.setFont(bodyFont, 10);
                stream.newLineAtOffset(margin, y);

                String[] lines = markdown != null ? markdown.split("\n") : new String[0];
                int linesRendered = 0;
                for (String rawLine : lines) {
                    if (linesRendered > 45) {
                        break; // Keep within single page for brief summary
                    }
                    String line = sanitizeText(rawLine.trim());
                    if (line.startsWith("#")) {
                        line = line.replaceAll("^#+\\s*", "");
                    }
                    if (line.length() > 85) {
                        line = line.substring(0, 82) + "...";
                    }
                    stream.showText(line);
                    stream.newLineAtOffset(0, -14);
                    linesRendered++;
                }

                stream.endText();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    public byte[] exportToDocx(String candidateName, String markdown, boolean factuallyCertified, int atsScore) throws IOException {
        try (XWPFDocument doc = new XWPFDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            // Header Title
            XWPFParagraph titlePara = doc.createParagraph();
            titlePara.setAlignment(ParagraphAlignment.LEFT);
            XWPFRun titleRun = titlePara.createRun();
            titleRun.setText((candidateName != null && !candidateName.isBlank()) ? candidateName : "Curriculum Vitae");
            titleRun.setBold(true);
            titleRun.setFontSize(20);
            titleRun.setFontFamily("Calibri");

            // Meta Stamp
            XWPFParagraph metaPara = doc.createParagraph();
            XWPFRun metaRun = metaPara.createRun();
            metaRun.setText(String.format("Rysoo ATS Alignment Score: %d%% | %s",
                    atsScore, factuallyCertified ? "Factually Certified Grounded" : "Flagged Inconsistencies"));
            metaRun.setItalic(true);
            metaRun.setFontSize(10);
            metaRun.setColor("666666");

            // Content lines
            if (markdown != null) {
                String[] lines = markdown.split("\n");
                for (String line : lines) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty()) {
                        continue;
                    }
                    XWPFParagraph para = doc.createParagraph();
                    XWPFRun run = para.createRun();
                    run.setFontFamily("Calibri");

                    if (trimmed.startsWith("### ")) {
                        run.setText(trimmed.substring(4));
                        run.setBold(true);
                        run.setFontSize(12);
                    } else if (trimmed.startsWith("## ")) {
                        run.setText(trimmed.substring(3));
                        run.setBold(true);
                        run.setFontSize(14);
                        run.setColor("2E3A59");
                    } else if (trimmed.startsWith("# ")) {
                        run.setText(trimmed.substring(2));
                        run.setBold(true);
                        run.setFontSize(16);
                    } else if (trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
                        run.setText("• " + trimmed.substring(2));
                        run.setFontSize(11);
                    } else {
                        run.setText(trimmed);
                        run.setFontSize(11);
                    }
                }
            }

            doc.write(baos);
            return baos.toByteArray();
        }
    }

    public String exportToJsonCertificate(String candidateName,
                                          String targetRole,
                                          int atsScore,
                                          int keywordCoverageScore,
                                          boolean factuallyCertified,
                                          List<String> employers,
                                          List<String> degrees,
                                          List<String> skills) {
        String timestamp = Instant.now().toString();
        String payloadToHash = String.format("%s|%s|%d|%d|%b|%s",
                candidateName, targetRole, atsScore, keywordCoverageScore, factuallyCertified, timestamp);
        String sha256 = calculateSha256(payloadToHash);

        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"certificate\": {\n");
        json.append("    \"platform\": \"Rysoo Anti-Hallucination ATS Platform\",\n");
        json.append("    \"version\": \"1.0.0\",\n");
        json.append(String.format("    \"timestamp\": \"%s\",\n", timestamp));
        json.append(String.format("    \"sha256VerificationHash\": \"%s\",\n", sha256));
        json.append(String.format("    \"candidateName\": \"%s\",\n", escapeJson(candidateName)));
        json.append(String.format("    \"targetRole\": \"%s\",\n", escapeJson(targetRole)));
        json.append(String.format("    \"atsScore\": %d,\n", atsScore));
        json.append(String.format("    \"keywordCoverageScore\": %d,\n", keywordCoverageScore));
        json.append(String.format("    \"factuallyCertified\": %b,\n", factuallyCertified));
        json.append("    \"verifiedEmployers\": [");
        appendJsonArray(json, employers);
        json.append("],\n");
        json.append("    \"verifiedDegrees\": [");
        appendJsonArray(json, degrees);
        json.append("],\n");
        json.append("    \"verifiedSkills\": [");
        appendJsonArray(json, skills);
        json.append("]\n");
        json.append("  }\n");
        json.append("}");

        return json.toString();
    }

    private void appendJsonArray(StringBuilder sb, List<String> items) {
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                sb.append("\"").append(escapeJson(items.get(i))).append("\"");
                if (i < items.size() - 1) {
                    sb.append(", ");
                }
            }
        }
    }

    private String sanitizeText(String text) {
        if (text == null) return "";
        // PDFBox Type 1 fonts only support standard WinAnsi characters
        return text.replaceAll("[^\\x20-\\x7E]", " ");
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"").replace("\n", " ");
    }

    private String calculateSha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            return "hash-unavailable";
        }
    }
}
