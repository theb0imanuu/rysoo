package com.rysoo.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cv_versions", uniqueConstraints = {
    @UniqueConstraint(name = "uq_cv_version", columnNames = {"cv_document_id", "version_number"})
})
public class CvVersionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cv_document_id", nullable = false)
    private CvDocumentEntity cvDocument;

    @Column(name = "version_number", nullable = false)
    private int versionNumber;

    @Column(name = "version_type", nullable = false, length = 32)
    private String versionType;

    @Column(name = "storage_path", length = 512)
    private String storagePath;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "mime_type", length = 128)
    private String mimeType;

    @Column(name = "content_hash_sha256", nullable = false, length = 64)
    private String contentHashSha256;

    @Column(name = "raw_text", columnDefinition = "TEXT")
    private String rawText;

    @Column(name = "normalized_markdown", nullable = false, columnDefinition = "TEXT")
    private String normalizedMarkdown;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public CvVersionEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public CvDocumentEntity getCvDocument() { return cvDocument; }
    public void setCvDocument(CvDocumentEntity cvDocument) { this.cvDocument = cvDocument; }

    public int getVersionNumber() { return versionNumber; }
    public void setVersionNumber(int versionNumber) { this.versionNumber = versionNumber; }

    public String getVersionType() { return versionType; }
    public void setVersionType(String versionType) { this.versionType = versionType; }

    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public Long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public String getContentHashSha256() { return contentHashSha256; }
    public void setContentHashSha256(String contentHashSha256) { this.contentHashSha256 = contentHashSha256; }

    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }

    public String getNormalizedMarkdown() { return normalizedMarkdown; }
    public void setNormalizedMarkdown(String normalizedMarkdown) { this.normalizedMarkdown = normalizedMarkdown; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
