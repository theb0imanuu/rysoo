package com.rysoo.application.service;

import com.rysoo.application.port.out.AiProviderPort;
import com.rysoo.domain.model.JobAnalysisResult;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class JobAnalysisService {

    private final AiProviderPort aiProviderPort;

    public JobAnalysisService(AiProviderPort aiProviderPort) {
        this.aiProviderPort = Objects.requireNonNull(aiProviderPort, "aiProviderPort must not be null");
    }

    public JobAnalysisResult analyze(String rawJobDescription) {
        if (rawJobDescription == null || rawJobDescription.trim().length() < 20) {
            throw new IllegalArgumentException("Job description must contain at least 20 characters of substantive text.");
        }
        return aiProviderPort.analyzeJobDescription(rawJobDescription);
    }
}
