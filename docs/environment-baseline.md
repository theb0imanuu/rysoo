# Environment Baseline — Rysoo CV Optimization Platform

**Audit Date**: 2026-10-20  
**Host Operating System**: Windows 11 Home/Pro (64-bit AMD64)  
**Shell**: PowerShell 5.1 / Windows Command Prompt (`cmd.exe`)  
**Workspace Root**: `C:\Users\MANU\Desktop\rysoo`  
**GitHub Remote**: `https://github.com/theb0imanuu/rysoo.git`

---

## 1. Installed Runtimes & Tooling Verification

| Tool / Runtime | Installed Version | Path / Verification Command | Status |
|---|---|---|---|
| **Node.js** | `v22.16.0` | `node -v` | Verified |
| **npm** | `11.6.0` | `npm -v` | Verified |
| **Java (JDK)** | `24.0.1` (Oracle JDK) | `java -version` | Verified (`C:\Program Files\Java\jdk-24`) |
| **Maven** | `Apache Maven 3.9.6` | `C:\Users\MANU\apache-maven-3.9.6\bin\mvn.cmd` | Verified (also configuring project Maven Wrapper) |
| **Git** | `2.53.0.windows.1` | `git --version` | Verified |
| **Docker Engine** | `29.1.2, build 890dcca` | `docker --version` | Verified (active daemon) |
| **GitHub CLI (`gh`)** | `gh.exe` | `C:\Program Files\GitHub CLI\gh.exe` | Verified (Logged in: `theb0imanuu`, scopes: `repo, workflow, gist, read:org`) |
| **Vercel CLI** | `48.1.1` | `vercel.ps1` | Verified (Logged in: `theb0imanuu`) |
| **Expo CLI** | Installed | `expo.ps1` | Verified |

---

## 2. Existing Workspace Assets (DO NOT OVERWRITE)

The following brand assets are present in the workspace root and will be utilized across web and mobile applications:
1. `favicon.ico` (432,254 bytes) — Multi-resolution platform icon
2. `logo.png` (24,206 bytes) — High-definition platform raster logo
3. `logo.svg` (8,523 bytes) — Scalable vector platform logo

---

## 3. External Accounts & Service Baseline

- **GitHub**: Authenticated as `theb0imanuu`. Git operations via HTTPS.
- **Vercel**: Authenticated as `theb0imanuu`. Ready for React frontend deployment link.
- **Render**: Render Blueprint (`render.yaml`) will configure the Spring Boot API container and background worker service. Deploy hooks / CI automation configured in Phase 4 and Phase 5.
- **Supabase**: Architecture follows Option A (Spring Boot is exclusive backend data layer; Supabase Auth issues JWT; Supabase Storage provides private buckets).
- **Docker**: Local Docker daemon active for building reproducible backend container images and running Testcontainers.

---

## 4. Initial Assumptions & Dependencies Checklist

- [x] Monorepo structure using standard Maven multi-module + npm workspaces layout.
- [x] Java backend containerized via multi-stage Dockerfile for direct deployment to Render.
- [x] Web frontend deployed to Vercel via standard Vite React build.
- [x] Option A architecture strictly enforced (no direct client table access in Supabase).
- [x] Test-driven development with reproducible unit, integration, and E2E suites.
- [x] Complete brand identity integration using the root logo and favicon files.
