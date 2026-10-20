package com.rysoo.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rysoo.api.dto.ExportDocumentRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void exportCv_unauthenticated_returns401() throws Exception {
        ExportDocumentRequest request = new ExportDocumentRequest(
                "Jane Doe",
                "# CV",
                "PDF",
                90,
                true,
                "Architect",
                92,
                List.of(),
                List.of(),
                List.of()
        );

        mockMvc.perform(post("/api/v1/cv/export")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void exportCv_pdfFormat_returns200AndPdfContentType() throws Exception {
        ExportDocumentRequest request = new ExportDocumentRequest(
                "Jane Doe",
                "# Jane Doe\nSenior Staff Engineer\n- Acme Corp",
                "PDF",
                94,
                true,
                "Cloud Architect",
                96,
                List.of("Acme Corp"),
                List.of("M.S. CS"),
                List.of("Java", "Docker")
        );

        mockMvc.perform(post("/api/v1/cv/export")
                        .with(jwt().jwt(j -> j.subject("00000000-0000-0000-0000-000000000001")))
                        .header("X-Correlation-ID", "test-corr-pdf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"Jane_Doe_Tailored_CV.pdf\""))
                .andExpect(header().string("X-Correlation-ID", "test-corr-pdf"));
    }

    @Test
    void exportCv_docxFormat_returns200AndDocxContentType() throws Exception {
        ExportDocumentRequest request = new ExportDocumentRequest(
                "Alex Chen",
                "# Alex Chen\nPrincipal Engineer",
                "DOCX",
                90,
                true,
                "Staff SRE",
                88,
                List.of("Nexus Cloud"),
                List.of("B.S. CE"),
                List.of("Kubernetes", "Go")
        );

        mockMvc.perform(post("/api/v1/cv/export")
                        .with(jwt().jwt(j -> j.subject("00000000-0000-0000-0000-000000000001")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"Alex_Chen_Tailored_CV.docx\""));
    }

    @Test
    void exportCv_jsonFormat_returns200AndJsonCertificate() throws Exception {
        ExportDocumentRequest request = new ExportDocumentRequest(
                "Jane Doe",
                "# Jane Doe",
                "JSON",
                95,
                true,
                "Cloud Architect",
                94,
                List.of("Acme Corp"),
                List.of("M.S. CS"),
                List.of("Java", "PostgreSQL")
        );

        mockMvc.perform(post("/api/v1/cv/export")
                        .with(jwt().jwt(j -> j.subject("00000000-0000-0000-0000-000000000001")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.certificate.candidateName").value("Jane Doe"))
                .andExpect(jsonPath("$.certificate.atsScore").value(95))
                .andExpect(jsonPath("$.certificate.sha256VerificationHash").isNotEmpty());
    }
}
