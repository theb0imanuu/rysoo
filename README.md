# Rysoo — Fact-Certified CV Optimization Platform

<p align="center">
  <img src="./logo.svg" alt="Rysoo Logo" width="96" height="96" />
</p>

<p align="center">
  <strong>Enterprise-grade CV tailoring engine with deterministic anti-hallucination verification, Spring Boot protected architecture, and durable background processing.</strong>
</p>

<p align="center">
  <a href="https://github.com/theb0imanuu/rysoo/actions/workflows/ci.yml"><img src="https://github.com/theb0imanuu/rysoo/actions/workflows/ci.yml/badge.svg" alt="CI Pipeline" /></a>
  <a href="https://github.com/theb0imanuu/rysoo/actions/workflows/deploy-api.yml"><img src="https://github.com/theb0imanuu/rysoo/actions/workflows/deploy-api.yml/badge.svg" alt="Deploy API" /></a>
  <a href="https://github.com/theb0imanuu/rysoo/actions/workflows/deploy-worker.yml"><img src="https://github.com/theb0imanuu/rysoo/actions/workflows/deploy-worker.yml/badge.svg" alt="Deploy Worker" /></a>
  <a href="#license"><img src="https://img.shields.io/badge/License-Proprietary-blue.svg" alt="License" /></a>
</p>

---

## 1. Architectural Invariants

### 🛡️ Option A Architecture Enforcement
* **Spring Boot is the exclusive data layer**: Web and mobile clients authenticate using Supabase Auth (JWT) and make requests **exclusively** to Spring Boot REST endpoints (`/api/v1/*`).
* **Direct client database access is strictly forbidden**: No client application queries or updates Supabase database tables directly.
* **Strict Tenant Isolation**: All endpoints resolve candidate data strictly through the authenticated JWT subject claim. Strangers attempting cross-tenant access receive HTTP 404 to avoid information disclosure.
* **Correlation Tracing**: Every client request attaches `X-Correlation-ID`, which propagates through Spring Boot logging (MDC) and background worker processing.

### 🔍 Deterministic Anti-Hallucination Invariant
* Unlike naive LLM resume wrappers that invent degrees, employers, or nonexistent technical skills to game ATS systems, Rysoo implements a **deterministic factual invariant gate**:
  * Source CVs are ingested and canonicalized into verified ground-truth facts (`CanonicalProfile`).
  * AI-generated CV tailoring is analyzed against ground truth before acceptance.
  * Any ungrounded employer, degree, or skill causes the claim to be flagged or rejected with detailed violation reports.
  * Every requirement is mapped to substantiated source quotes with confidence ratings (`DEMONSTRATED`, `PARTIALLY_SUPPORTED`, `MISSING`).

---

## 2. Monorepo Structure

```
rysoo/
├── .github/workflows/          # Automated GitHub Actions (CI, Deploy API, Deploy Worker)
├── apps/
│   ├── api/                    # Spring Boot 3.3.4 REST API (OAuth2 Resource Server, Flyway)
│   ├── worker/                 # Spring Boot Background Worker (Durable queue poller, reaper)
│   ├── web/                    # React 18 + Vite Web App (Dark glassmorphism UI, Diff, Audit)
│   └── mobile/                 # React Native + Expo 51 Mobile Application
├── services/
│   ├── cv-domain/              # Domain models, anti-hallucination verifier, evidence mappings
│   ├── cv-application/         # Application orchestration, AI provider ports, job analysis
│   ├── cv-infrastructure/      # PostgreSQL JPA entities, Flyway migrations, durable job queue
│   └── cv-document-processing/ # Apache Tika/PDFBox/POI ingestion, normalizer, export service
├── packages/
│   └── contracts/              # Shared TypeScript data contracts and API envelopes
├── supabase/migrations/        # Flyway & Supabase idempotent schema migrations
├── Dockerfile                  # Hardened multi-stage build (JRE 21 minimal, unprivileged user)
├── render.yaml                 # Render Blueprint for API, Worker, and PostgreSQL
└── pom.xml                     # Maven parent POM (Java 21 / 24, Spring Boot 3.3.4)
```

---

## 3. Deploying the Backend Container to Render via CI/CD

The platform includes full automation for deploying the containerized backend to **Render** using a multi-stage `Dockerfile`, `render.yaml` Blueprint, and GitHub Actions CI/CD workflows.

### A. Deployment Topology
1. **`rysoo-api` (Render Web Service)**: Runs the Spring Boot REST API (`apps/api`) listening on port 8080 with live health checks at `/actuator/health`.
2. **`rysoo-worker` (Render Background Worker)**: Runs the background worker daemon (`apps/worker`), polling the durable database queue `processing_jobs` with row-level locks, exponential backoff, and abandoned job recovery.
3. **Managed PostgreSQL**: Backing database managed by Render or Supabase, provisioned automatically via Flyway migrations on startup.

### B. CI/CD Pipeline Flow (`.github/workflows/`)
* **`ci.yml`**: On every push and PR to `main`:
  1. Compiles and executes all 43 backend Java tests.
  2. Verifies the multi-stage Docker build.
  3. Typechecks and runs tests for `@rysoo/contracts`, `apps/web` (Vitest), and `apps/mobile`.
* **`deploy-api.yml`**: On push to `main` touching API or services:
  1. Builds Docker container targeting `runtime-api`.
  2. Pushes image to GitHub Container Registry (`ghcr.io/theb0imanuu/rysoo/api`).
  3. Triggers Render Web Service deploy hook (`RENDER_API_DEPLOY_HOOK_URL`).
* **`deploy-worker.yml`**: On push to `main` touching worker or services:
  1. Builds Docker container targeting `runtime-worker`.
  2. Pushes image to GitHub Container Registry (`ghcr.io/theb0imanuu/rysoo/worker`).
  3. Triggers Render Background Worker deploy hook (`RENDER_WORKER_DEPLOY_HOOK_URL`).

### C. Deploying With Render Blueprint (`render.yaml`)
1. In your Render Dashboard, click **New +** &rarr; **Blueprint**.
2. Connect your GitHub repository `https://github.com/theb0imanuu/rysoo.git`.
3. Render reads `render.yaml` and provisions both services automatically.
4. Set your production secrets in Render or GitHub repository secrets:
   - `RENDER_API_DEPLOY_HOOK_URL` (from Render service settings)
   - `RENDER_WORKER_DEPLOY_HOOK_URL` (from Render worker settings)
   - `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
   - `SUPABASE_URL`, `SUPABASE_JWT_ISSUER`, `SUPABASE_SERVICE_ROLE_KEY`
   - `OPENAI_API_KEY` (or AI provider key)

---

## 4. Local Development Quickstart

### Prerequisites
* JDK 21+ (`java -version`)
* Apache Maven 3.9+ (`mvn -version`)
* Node.js 20+ and npm 10+ (`node -v`, `npm -v`)
* Docker (`docker --version`)

### 1. Run All Tests
```bash
# Run all backend Java tests (Domain, Application, Infrastructure, Processing, API, Worker)
mvn clean test

# Run frontend web tests
npm --prefix apps/web run test

# Run mobile tests
npm --prefix apps/mobile run test
```

### 2. Start the Backend API Locally
```bash
mvn --projects apps/api spring-boot:run
# API runs at http://localhost:8080
# OpenAPI Swagger UI: http://localhost:8080/swagger-ui.html
# Health Probe: http://localhost:8080/api/v1/health
```

### 3. Start the Background Worker Locally
```bash
mvn --projects apps/worker spring-boot:run
```

### 4. Start the Web Application
```bash
npm --prefix apps/web run dev
# Web application available at http://localhost:5173
```

### 5. Start the Mobile Application
```bash
npm --prefix apps/mobile run start
# Scan QR code with Expo Go on iOS or Android
```

---

## 5. Security & Deliverables

* **Zero-Hallucination Audit Certificate**: Cryptographically verifiable JSON certificate with SHA-256 integrity hash confirming all claimed facts are grounded.
* **Document Exports**: Clean PDF via Apache PDFBox, Word (.docx) via Apache POI, and standard Markdown (.md).
* **Immutable Version History**: Full version rollback support creating traceable historical version audits.
