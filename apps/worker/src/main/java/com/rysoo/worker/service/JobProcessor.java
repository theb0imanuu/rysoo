package com.rysoo.worker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rysoo.application.service.CvOptimizationService;
import com.rysoo.docprocessing.DocumentIngestionService;
import com.rysoo.domain.model.CanonicalProfile;
import com.rysoo.domain.model.OptimizationOutput;
import com.rysoo.infrastructure.persistence.entity.ProcessingJobEntity;
import com.rysoo.infrastructure.queue.JobQueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Service
public class JobProcessor {

    private static final Logger log = LoggerFactory.getLogger(JobProcessor.class);

    private final JobQueueService jobQueueService;
    private final CvOptimizationService cvOptimizationService;
    private final DocumentIngestionService documentIngestionService;
    private final ObjectMapper objectMapper;

    public JobProcessor(
            JobQueueService jobQueueService,
            CvOptimizationService cvOptimizationService,
            DocumentIngestionService documentIngestionService,
            ObjectMapper objectMapper
    ) {
        this.jobQueueService = jobQueueService;
        this.cvOptimizationService = cvOptimizationService;
        this.documentIngestionService = documentIngestionService;
        this.objectMapper = objectMapper;
    }

    public void processJob(ProcessingJobEntity job) {
        log.info("Processing job {} of type {}", job.getId(), job.getJobType());
        try {
            switch (job.getJobType()) {
                case "CV_OPTIMIZATION" -> processCvOptimization(job);
                case "DOCUMENT_INGESTION" -> processDocumentIngestion(job);
                default -> throw new IllegalArgumentException("Unsupported job type: " + job.getJobType());
            }
        } catch (Exception e) {
            log.error("Error executing job {}: {}", job.getId(), e.getMessage(), e);
            jobQueueService.markFailed(job.getId(), e.getMessage());
        }
    }

    private void processCvOptimization(ProcessingJobEntity job) throws Exception {
        JsonNode root = objectMapper.readTree(job.getPayloadJson());

        String rawJobDescription = root.path("jobDescription").asText();
        JsonNode profileNode = root.path("profile");

        String name = profileNode.path("candidateName").asText("Candidate");
        String email = profileNode.path("email").asText("");
        String phone = profileNode.path("phone").asText("");
        String location = profileNode.path("location").asText("");
        String summary = profileNode.path("summary").asText("");

        Set<String> employers = new HashSet<>();
        if (profileNode.has("employers")) {
            profileNode.path("employers").forEach(e -> employers.add(e.asText()));
        }

        Set<String> degrees = new HashSet<>();
        if (profileNode.has("degrees")) {
            profileNode.path("degrees").forEach(d -> degrees.add(d.asText()));
        }

        Set<String> skills = new HashSet<>();
        if (profileNode.has("skills")) {
            profileNode.path("skills").forEach(s -> skills.add(s.asText()));
        }

        CanonicalProfile canonicalProfile = new CanonicalProfile(
                name, email, phone, location, summary,
                Collections.unmodifiableSet(employers),
                Collections.unmodifiableSet(degrees),
                Collections.unmodifiableSet(skills),
                Collections.emptyList()
        );

        OptimizationOutput output = cvOptimizationService.optimize(canonicalProfile, rawJobDescription);

        String resultJson = objectMapper.writeValueAsString(output);
        jobQueueService.markCompleted(job.getId(), resultJson);
    }

    private void processDocumentIngestion(ProcessingJobEntity job) throws Exception {
        JsonNode root = objectMapper.readTree(job.getPayloadJson());
        String filename = root.path("filename").asText("document.md");
        byte[] bytes = root.path("contentBase64").binaryValue();

        var result = documentIngestionService.ingest(bytes, filename);
        String resultJson = objectMapper.writeValueAsString(result);
        jobQueueService.markCompleted(job.getId(), resultJson);
    }
}
