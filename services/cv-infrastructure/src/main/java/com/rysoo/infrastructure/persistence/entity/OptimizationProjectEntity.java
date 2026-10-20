package com.rysoo.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "optimization_projects")
public class OptimizationProjectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cv_document_id", nullable = false)
    private CvDocumentEntity cvDocument;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_description_id", nullable = false)
    private JobDescriptionEntity jobDescription;

    @Column(nullable = false)
    private String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public OptimizationProjectEntity() {}

    public OptimizationProjectEntity(UUID userId, CvDocumentEntity cvDocument, JobDescriptionEntity jobDescription, String name) {
        this.userId = userId;
        this.cvDocument = cvDocument;
        this.jobDescription = jobDescription;
        this.name = name;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public CvDocumentEntity getCvDocument() { return cvDocument; }
    public void setCvDocument(CvDocumentEntity cvDocument) { this.cvDocument = cvDocument; }

    public JobDescriptionEntity getJobDescription() { return jobDescription; }
    public void setJobDescription(JobDescriptionEntity jobDescription) { this.jobDescription = jobDescription; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
