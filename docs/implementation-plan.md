# CV Optimization Platform (Rysoo) — Implementation Plan

## Executive Overview

This plan defines the step-by-step roadmap to build, test, containerize, and deploy the **Rysoo CV Optimization Platform** across all 17 phases defined in the Master Engineering Prompt.

The platform enables users to register, upload CVs in PDF or DOCX format, extract content into clean structured Markdown, compare with job descriptions, perform evidence mapping, generate tailored CVs without hallucinating qualifications, review and edit diffs, and export to PDF/DOCX/Markdown.

The architecture enforces **Option A**: Spring Boot is the exclusive data access layer. Web (React + Vite) and Mobile (React Native + Expo) authenticate via Supabase Auth and communicate exclusively with the Spring Boot REST API using validated JWTs. The backend runs in a production-ready Docker container deployable to Render with background worker processing and CI/CD via GitHub Actions.

---

## Phase Breakdown & Gates

### Phase 0: Workspace Discovery and Engineering Baseline

- **Goal**: Audit environment, detect runtimes, verify git repo and remotes, record baseline.
- **Deliverables**: `docs/progress.md`, `docs/environment-baseline.md`, `docs/implementation-plan.md`.
- **Gate 0 Verification**: Workspace state validated, git remote `https://github.com/theb0imanuu/rysoo.git` configured, baseline tools confirmed.

### Phase 1: Install ECC and Configure the Coding Environment

- **Goal**: Inspect and run `npx ecc-install --target antigravity`, configure TypeScript, Java, and Spring Boot capabilities, create `AGENTS.md` and `.editorconfig`.
- **Deliverables**: Installed ECC configs, `AGENTS.md`, `.editorconfig`.
- **Gate 1 Verification**: ECC installation verified or blocker documented with exact alternative, coding standards codified.

### Phase 2: Architecture and Acceptance Criteria

- **Goal**: Author full system specifications, data models, OpenAPI contracts, and testable acceptance criteria.
- **Deliverables**: `docs/architecture.md`, `docs/api.md`, `docs/security.md`, `docs/testing.md`, `docs/data-model.md`, `docs/deployment.md`, `docs/operations.md`.
- **Gate 2 Verification**: Complete specifications reviewed before any feature implementation.

### Phase 3: Establish Test Strategy and Test Infrastructure

- **Goal**: Build runnable test scaffolding, synthetic fixtures (PDF, DOCX, Job Descriptions, malformed files), JUnit 5, Vitest, and Jest configurations.
- **Deliverables**: `tests/fixtures/`, test suites with initial failing/scaffolding tests.
- **Gate 3 Verification**: Test infrastructure runs locally; expected failures documented.

### Phase 4: Configure GitHub and CI/CD Before Application Coding

- **Goal**: Set up GitHub Actions workflows (`ci.yml`, `deploy-api.yml`, `deploy-worker.yml`, `mobile-build.yml`, `security.yml`, `dependabot.yml`).
- **Deliverables**: `.github/workflows/`, `.github/dependabot.yml`.
- **Gate 4 Verification**: CI checks validate monorepo integrity and change detection across modules.

### Phase 5: Hosting Configuration and Deployment Automation

- **Goal**: Author root `render.yaml` Blueprint, multi-stage backend `Dockerfile` (API & worker entry points), Vercel configuration for web, Expo `eas.json`, Supabase configuration and `.env.example`.
- **Deliverables**: `Dockerfile`, `.dockerignore`, `render.yaml`, `apps/web/vercel.json`, `apps/mobile/eas.json`, `.env.example`.
- **Gate 5 Verification**: Docker build builds cleanly; Render Blueprint validates.

### Phase 6: Database Schema and Persistence

- **Goal**: Implement Flyway migrations, PostgreSQL entity models, safe concurrency, and tested deletion strategy.
- **Deliverables**: `supabase/migrations/`, JPA entities and Spring Data repositories in `services/cv-infrastructure`.
- **Gate 6 Verification**: Migrations apply cleanly to PostgreSQL; repository persistence tests pass.

### Phase 7: Authentication and Backend Foundation

- **Goal**: Spring Boot core configuration, Spring Security OAuth2 resource server validating Supabase JWTs, ownership validation, correlation IDs, rate limits, global error handling, OpenAPI docs.
- **Deliverables**: `apps/api/`, security filters, exception handlers, health endpoints.
- **Gate 7 Verification**: JWT validation, expired/invalid token rejection, and resource authorization tests pass.

### Phase 8: Document Ingestion and Markdown Normalization

- **Goal**: Document ingestion module with Apache Tika, PDFBox, Apache POI, OCR fallback, structured Markdown normalization, and provenance preservation.
- **Deliverables**: `services/cv-document-processing/`.
- **Gate 8 Verification**: Parsing of synthetic PDFs, DOCXs, and malformed files passes with fact preservation.

### Phase 9: AI-Assisted Job Analysis and CV Optimization

- **Goal**: Provider-agnostic AI interface, structured requirement extraction, evidence mapping (demonstrated vs inferred vs missing), tailored CV generation, strict factual verification (no hallucinations).
- **Deliverables**: `services/cv-domain/`, `services/cv-application/`.
- **Gate 9 Verification**: Factual-consistency tests pass; hallucinations are deterministically caught and flagged.

### Phase 10: Durable Background Processing

- **Goal**: PostgreSQL-backed durable job queue with atomic state transitions (`QUEUED`, `PROCESSING`, `RETRYING`, `COMPLETED`, `FAILED`), status polling endpoint, worker process.
- **Deliverables**: `apps/worker/`, queue management service.
- **Gate 10 Verification**: Queue tests verify duplicate submission prevention, retry limits, and crash recovery.

### Phase 11: React Web Application

- **Goal**: Responsive React + Vite + TypeScript web application with rich aesthetics, brand logo/favicon integration, upload, Markdown editor, job analyzer, optimization diff comparison, and export controls.
- **Deliverables**: `apps/web/`, OpenAPI generated client in `packages/api-client`.
- **Gate 11 Verification**: Web unit tests and Playwright E2E tests pass.

### Phase 12: React Native Mobile Application

- **Goal**: Expo-based React Native mobile application sharing contracts and validation schemas with web, featuring Supabase Auth, secure token storage, CV upload, and review.
- **Deliverables**: `apps/mobile/`.
- **Gate 12 Verification**: Mobile tests and Expo build validation pass.

### Phase 13: Export and Version History

- **Goal**: Multi-format export engine (PDF, DOCX, Markdown), version recovery, authorized download verification.
- **Deliverables**: Export services and download controllers.
- **Gate 13 Verification**: Export validity and authorization tests pass.

### Phase 14: Integrated Quality Assurance

- **Goal**: Full integration test execution across backend, web, mobile, E2E, Docker build, and security scans.
- **Deliverables**: Test execution reports, security scan reports.
- **Gate 14 Verification**: All suites pass; residual risks documented.

### Phase 15 & 16: Staging and Production Deployment Automation

- **Goal**: Automated deployment via GitHub Actions to Render (Dockerized API & Worker) and Vercel (Web frontend), staging smoke tests, health check verification, rollback runbooks.
- **Deliverables**: Verified deployment scripts, health check validations.
- **Gate 15/16 Verification**: Verification of live endpoints and release checklists.

### Phase 17: Documentation and Handover

- **Goal**: Comprehensive project documentation, runbooks, architecture diagrams, and final implementation report.
- **Deliverables**: Final README, runbooks, verified operational guides.
- **Gate 17 Verification**: Complete handover package approved.
