package com.rysoo.application;

import com.rysoo.application.adapter.MockAiProviderAdapter;
import com.rysoo.application.service.JobAnalysisService;
import com.rysoo.domain.model.JobAnalysisResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JobAnalysisServiceTest {

    private JobAnalysisService jobAnalysisService;

    @BeforeEach
    void setUp() {
        jobAnalysisService = new JobAnalysisService(new MockAiProviderAdapter());
    }

    @Test
    @DisplayName("Should extract structured requirements from standard job description")
    void analyzeStandardJobDescription() throws IOException {
        String jdContent = Files.readString(Path.of("../../tests/fixtures/job-description-standard.txt"));

        JobAnalysisResult result = jobAnalysisService.analyze(jdContent);

        assertThat(result).isNotNull();
        assertThat(result.roleTitle()).contains("Senior Backend Engineer");
        assertThat(result.seniorityLevel()).isEqualTo("Senior");
        assertThat(result.requiredSkills()).contains("Java", "Spring Boot", "PostgreSQL", "Docker");
        assertThat(result.preferredSkills()).contains("AWS");
        assertThat(result.coreResponsibilities()).isNotEmpty();
    }

    @Test
    @DisplayName("Should reject empty or whitespace job descriptions")
    void rejectEmptyJobDescription() {
        assertThatThrownBy(() -> jobAnalysisService.analyze(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least 20 characters");

        assertThatThrownBy(() -> jobAnalysisService.analyze("   \n\t   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least 20 characters");
    }

    @Test
    @DisplayName("Should reject job descriptions that are too short")
    void rejectTooShortJobDescription() {
        assertThatThrownBy(() -> jobAnalysisService.analyze("Hiring dev"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least 20 characters");
    }
}
