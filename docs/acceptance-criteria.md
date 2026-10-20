# Explicit Acceptance Criteria — Rysoo CV Optimization Platform

This document codifies testable acceptance criteria across the core user journeys and technical capabilities.

---

### AC-1: Registration & Authentication
- **Given** an unauthenticated visitor on Web or Mobile,
- **When** the visitor registers or logs in via Supabase Auth,
- **Then** a valid JWT is issued containing `sub`, `email`, and `exp`.
- **When** the client sends the token to any protected `/api/v1/*` endpoint,
- **Then** the Spring Boot API verifies signature, issuer, and expiry and returns `200 OK`.
- **When** an invalid or expired token is sent,
- **Then** the API returns `401 Unauthorized` with a standard error envelope.

### AC-2: CV Upload & Intent
- **Given** an authenticated user,
- **When** the user requests an upload intent for a PDF or DOCX file <= 10MB,
- **Then** the API validates the file type and generates a signed storage upload URL and returns `201 Created` with a new `cvDocumentId`.
- **When** a user attempts to upload a file > 10MB or an invalid extension (.exe, .zip),
- **Then** the API rejects the request with `400 Bad Request`.

### AC-3: Document Parsing & Signature Verification
- **Given** an uploaded file in private storage,
- **When** the document ingestion worker processes the file,
- **Then** Apache Tika verifies the actual binary magic bytes (`%PDF-` or OOXML ZIP header).
- **If** the file content does not match its claimed MIME type or is corrupted,
- **Then** the job fails with status `PARSE_FAILED` and actionable error details without exposing system stack traces.

### AC-4: Markdown Normalization
- **Given** a valid PDF or DOCX file,
- **When** parsing completes,
- **Then** text is normalized into standard Markdown with preserved structure (Contact Info, Summary, Experience with dates and employers, Education, Skills, and Bullet points).
- **Then** a canonical AST JSON is generated and stored in `cv_versions`.

### AC-5: Markdown Editing
- **Given** a normalized CV version,
- **When** the user modifies the Markdown text in the editor and submits,
- **Then** a new version with `version_type = 'MANUALLY_EDITED'` and incremented `version_number` is created.
- **Then** the previous version remains intact in history.

### AC-6: Job Description Analysis
- **Given** a raw job description text,
- **When** the user submits the job description,
- **Then** the AI analyzer extracts structured requirements categorized into:
  - Role overview and responsibilities.
  - Required qualifications & years of experience.
  - Core technical and soft skills.
  - Specific domain terminology and tooling.

### AC-7: Evidence Mapping
- **Given** a normalized CV and parsed job requirements,
- **When** the evidence mapping engine executes,
- **Then** each job requirement is mapped to one of three categories:
  1. `DEMONSTRATED`: Explicitly substantiated by source CV facts with quotes.
  2. `PARTIALLY_SUPPORTED`: Transferable experience requiring user confirmation.
  3. `MISSING`: No factual evidence present in the source CV.

### AC-8: Optimization Engine (Anti-Hallucination)
- **Given** mapped evidence and source CV,
- **When** the optimization engine generates a tailored CV,
- **Then** it must **never** fabricate:
  - Employers or job titles.
  - Employment dates or durations.
  - Degrees or educational institutions.
  - Certifications or awards.
  - Technologies or metrics not supported by the source CV.

### AC-9: Validation of Generated Claims
- **Given** a generated tailored CV,
- **When** the deterministic fact checker validates the output,
- **Then** every employer, date, and metric is verified against source CV facts.
- **If** an unsupported fact is detected,
- **Then** the output is flagged with `FLAGGED_INCONSISTENCIES` and highlights the disputed claim for user review.

### AC-10: Export Generation
- **Given** an accepted optimized or edited CV,
- **When** the user requests an export in PDF, DOCX, or Markdown format,
- **Then** the worker generates structurally valid documents.
- **Then** PDF exports render with clean typography, proper page margins, and no cut-off lines.
- **Then** DOCX exports open in Microsoft Word and LibreOffice without formatting corruption.

### AC-11: History & Versioning
- **Given** multiple optimization runs or edits,
- **When** the user views the document history,
- **Then** all previous versions, job descriptions, and optimization diffs are viewable and restorable.

### AC-12: Usage Tracking & Cost Estimation
- **Given** an optimization run,
- **When** the run completes,
- **Then** token usage (input/output) and estimated USD costs are recorded in `usage_ledger`.

### AC-13: Deletion & Privacy
- **Given** a user request to delete a CV document or their account,
- **When** deletion is executed,
- **Then** all database rows and associated private storage objects are deleted permanently.
- **Then** subsequent requests for the deleted document return `404 Not Found`.

### AC-14: Mobile Functionality
- **Given** the React Native Expo mobile application,
- **When** a user logs in,
- **Then** the mobile app connects to the same Spring Boot backend, displays the same CVs, allows PDF/DOCX upload, and views optimization results and gap analyses.

### AC-15: Deployment & Crash Recovery
- **Given** a sudden worker process restart or kill while a job is `PROCESSING`,
- **When** the background reaper runs,
- **Then** the abandoned job is detected, reclaimed, and retried automatically up to `max_retries`.
