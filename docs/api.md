# REST API Specification (/api/v1) — Rysoo CV Optimization Platform

All endpoints require `Authorization: Bearer <Supabase_JWT>` unless marked public.  
Standard response envelope:
```json
{
  "success": true,
  "data": { ... },
  "error": null,
  "meta": {
    "correlationId": "corr-uuid-1234",
    "timestamp": "2026-10-20T12:00:00Z"
  }
}
```

Standard error response envelope:
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "RESOURCE_NOT_FOUND",
    "message": "CV Document not found or not owned by user.",
    "details": []
  },
  "meta": {
    "correlationId": "corr-uuid-1234",
    "timestamp": "2026-10-20T12:00:00Z"
  }
}
```

---

## 1. Authentication & Profile Endpoints

### `GET /api/v1/profile`
Retrieve authenticated user profile.
- **Response**: `{ id, email, fullName, createdAt }`

### `PUT /api/v1/profile`
Update user profile details.
- **Body**: `{ fullName: string }`

---

## 2. CV Ingestion & Document Endpoints

### `POST /api/v1/cvs/upload-intent`
Request an authorized pre-signed upload URL for direct client upload to Supabase private bucket.
- **Body**:
  ```json
  {
    "fileName": "resume.pdf",
    "mimeType": "application/pdf",
    "fileSizeBytes": 1048576
  }
  ```
- **Response**:
  ```json
  {
    "cvDocumentId": "uuid",
    "cvVersionId": "uuid",
    "uploadUrl": "https://<supabase-storage-signed-put-url>",
    "storagePath": "user_id/cv_id/resume.pdf",
    "expiresInSeconds": 900
  }
  ```

### `POST /api/v1/cvs/{id}/confirm-upload`
Notify backend that the binary was uploaded to storage; triggers asynchronous document parsing and normalization.
- **Response**: `{ cvDocumentId, status: "PROCESSING", jobId: "job-uuid" }`

### `GET /api/v1/cvs`
List all CV documents owned by authenticated user.
- **Query Params**: `page=0&size=20&sort=createdAt,desc`
- **Response**: Paginated list of CV document summaries.

### `GET /api/v1/cvs/{id}`
Retrieve CV document details and versions.
- **Response**: CV document detail including current active version, normalized Markdown, and version history.

### `PUT /api/v1/cvs/{id}/versions/{versionId}/markdown`
Save user edits to normalized Markdown, creating a new `MANUALLY_EDITED` version.
- **Body**: `{ markdown: string }`
- **Response**: New CV version detail.

### `DELETE /api/v1/cvs/{id}`
Permanently delete CV document and associated files.
- **Response**: `{ message: "CV document deleted successfully" }`

---

## 3. Job Description Endpoints

### `POST /api/v1/jobs`
Create and analyze a job description.
- **Body**:
  ```json
  {
    "targetRole": "Senior Full-Stack Engineer",
    "targetCompany": "Acme Corp",
    "rawText": "Job description text..."
  }
  ```
- **Response**: Analyzed job description entity with extracted responsibilities, required skills, and qualifications.

### `GET /api/v1/jobs`
List user's saved job descriptions.

### `GET /api/v1/jobs/{id}`
Get job description details and parsed requirement breakdown.

---

## 4. Optimization Engine Endpoints

### `POST /api/v1/optimizations`
Initiate an asynchronous CV optimization run against a job description.
- **Header**: `Idempotency-Key: <unique-uuid>`
- **Body**:
  ```json
  {
    "cvVersionId": "uuid",
    "jobDescriptionId": "uuid",
    "projectName": "Acme Senior Engineer Application"
  }
  ```
- **Response**:
  ```json
  {
    "projectId": "uuid",
    "runId": "uuid",
    "jobId": "uuid",
    "status": "QUEUED"
  }
  ```

### `GET /api/v1/optimizations/runs/{runId}`
Poll optimization run status, progress, and results.
- **Response**:
  ```json
  {
    "runId": "uuid",
    "status": "COMPLETED",
    "attemptCount": 1,
    "result": {
      "matchScorePercentage": 88,
      "matchExplanation": "Strong alignment in distributed systems and Spring Boot...",
      "tailoredMarkdown": "# John Doe\n...",
      "evidenceMapping": {
        "demonstrated": [
          {"requirement": "5+ years Java", "evidence": "7 years backend engineering at TechCorp"}
        ],
        "partiallySupported": [
          {"requirement": "Kubernetes operator development", "evidence": "Experience deploying Docker to K8s clusters"}
        ],
        "missing": [
          {"requirement": "Rust development", "evidence": null}
        ]
      },
      "factualVerificationStatus": "PASSED"
    }
  }
  ```

---

## 5. Export Endpoints

### `POST /api/v1/exports`
Queue generation of an export file (PDF, DOCX, or Markdown).
- **Body**:
  ```json
  {
    "optimizationResultId": "uuid",
    "format": "PDF"
  }
  ```
- **Response**: `{ exportId: "uuid", status: "PROCESSING", jobId: "uuid" }`

### `GET /api/v1/exports/{id}/download-url`
Obtain a short-lived, ownership-verified signed download URL for an export file.
- **Response**: `{ downloadUrl: "https://...", expiresInSeconds: 300 }`

---

## 6. Jobs & Diagnostics

### `GET /api/v1/jobs/status/{jobId}`
Query status of any background processing job.
- **Response**: `{ jobId, status: "COMPLETED", progressPercentage: 100 }`

### `GET /actuator/health` (Public)
Health check liveness and readiness probes for Render.
