import React from 'react';
import { Server, Shield, Database, Cpu, Layers, GitBranch, CheckCircle2 } from 'lucide-react';

export const Architecture: React.FC = () => {
  return (
    <div className="architecture-page">
      <div className="architecture-header glass-panel">
        <div className="arch-header-left">
          <Layers size={28} className="text-primary" />
          <div>
            <h2>Option A System Architecture</h2>
            <p className="arch-subtitle">
              Spring Boot as the exclusive data access layer with deterministic anti-hallucination guarantees
            </p>
          </div>
        </div>
        <div className="arch-header-right">
          <span className="badge badge-success">
            <CheckCircle2 size={14} />
            Render-Ready Containerized Stack
          </span>
        </div>
      </div>

      <div className="arch-grid">
        {/* Tier 1: Client Layer */}
        <div className="arch-card glass-panel">
          <div className="arch-card-header">
            <div className="card-icon-wrapper primary">
              <Cpu size={20} />
            </div>
            <div>
              <h4>Tier 1: Client Applications</h4>
              <span className="card-subtext">Web (React 18 + Vite) & Mobile (Expo)</span>
            </div>
          </div>
          <div className="arch-card-body">
            <ul className="arch-feature-list">
              <li>
                <strong>Supabase Auth Integration:</strong> Users authenticate via Supabase Auth and obtain standard JWT bearer tokens.
              </li>
              <li>
                <strong>Option A Rule Enforcement:</strong> Clients NEVER query or update Supabase database tables directly.
              </li>
              <li>
                <strong>Typed API Contract:</strong> All mutations and queries invoke Spring Boot REST endpoints (<code className="code-pill">/api/v1/*</code>) carrying the Supabase JWT.
              </li>
              <li>
                <strong>Correlation IDs:</strong> Every outbound client request automatically attaches a unique <code className="code-pill">X-Correlation-ID</code> header.
              </li>
            </ul>
          </div>
        </div>

        {/* Tier 2: Spring Boot API */}
        <div className="arch-card glass-panel">
          <div className="arch-card-header">
            <div className="card-icon-wrapper purple">
              <Server size={20} />
            </div>
            <div>
              <h4>Tier 2: Spring Boot API Gateway</h4>
              <span className="card-subtext">Enterprise Security & Orchestration</span>
            </div>
          </div>
          <div className="arch-card-body">
            <ul className="arch-feature-list">
              <li>
                <strong>OAuth2 Resource Server:</strong> Validates Supabase JWTs locally using RS256 JWKS or HMAC public keys.
              </li>
              <li>
                <strong>Strict Tenant Isolation:</strong> Every request is scoped to the authenticated user's UUID; strangers receive 404/403.
              </li>
              <li>
                <strong>Idempotent Enqueueing:</strong> Optimizations are enqueued with unique idempotency keys via <code className="code-pill">POST /api/v1/jobs/cv-optimization</code> returning HTTP 202 Accepted.
              </li>
              <li>
                <strong>Flyway Migrations:</strong> Spring Boot manages database schema evolution and audit logs.
              </li>
            </ul>
          </div>
        </div>

        {/* Tier 3: Zero-Hallucination Domain Engine */}
        <div className="arch-card glass-panel">
          <div className="arch-card-header">
            <div className="card-icon-wrapper success">
              <Shield size={20} />
            </div>
            <div>
              <h4>Tier 3: Domain Invariant Engine</h4>
              <span className="card-subtext">services/cv-domain & cv-application</span>
            </div>
          </div>
          <div className="arch-card-body">
            <ul className="arch-feature-list">
              <li>
                <strong>Canonical Fact Extraction:</strong> Normalized CVs are indexed into verified employers, degrees, metrics, and skills.
              </li>
              <li>
                <strong>Deterministic Anti-Hallucination Gate:</strong> Verifies every claimed item against candidate ground truth before certifying.
              </li>
              <li>
                <strong>Evidence Substantiation:</strong> Maps each job description requirement to verified quotes from the source document.
              </li>
              <li>
                <strong>Strict Invariant:</strong> Any unsupported claim or hallucinated employer/degree causes immediate rejection and flagging.
              </li>
            </ul>
          </div>
        </div>

        {/* Tier 4: Background Processing & Cloud Deployment */}
        <div className="arch-card glass-panel">
          <div className="arch-card-header">
            <div className="card-icon-wrapper amber">
              <Database size={20} />
            </div>
            <div>
              <h4>Tier 4: Durable Worker & Cloud</h4>
              <span className="card-subtext">apps/worker & Render Blueprint</span>
            </div>
          </div>
          <div className="arch-card-body">
            <ul className="arch-feature-list">
              <li>
                <strong>Durable Job Queue:</strong> Spring Boot worker polls <code className="code-pill">processing_jobs</code> using transaction-safe row locks.
              </li>
              <li>
                <strong>Exponential Backoff:</strong> Automatic retries with jitter and dead-letter handling.
              </li>
              <li>
                <strong>Abandoned Job Reaper:</strong> Recovers jobs if a worker node crashes mid-flight.
              </li>
              <li>
                <strong>Render & Docker:</strong> Multi-stage Docker container deployed to Render Web Service and Background Worker via GitHub Actions CI/CD.
              </li>
            </ul>
          </div>
        </div>
      </div>

      {/* CI/CD Pipeline Flowchart */}
      <div className="cicd-section glass-panel">
        <div className="section-header">
          <div className="flex items-center gap-2">
            <GitBranch size={20} className="text-primary" />
            <h4>Continuous Integration & Deployment Pipeline</h4>
          </div>
          <span className="badge badge-primary">GitHub Actions &rarr; Render</span>
        </div>
        <div className="pipeline-steps-row">
          <div className="pipe-step">
            <div className="pipe-number">01</div>
            <div className="pipe-title">Monorepo CI</div>
            <div className="pipe-desc">Maven tests (Domain, App, Infra, API, Worker) + Vitest (Web, Contracts)</div>
          </div>
          <div className="pipe-arrow">&rarr;</div>
          <div className="pipe-step">
            <div className="pipe-number">02</div>
            <div className="pipe-title">Docker Multi-Stage Build</div>
            <div className="pipe-desc">Builds minimal JRE 21 runtime image with unprivileged rysoo user</div>
          </div>
          <div className="pipe-arrow">&rarr;</div>
          <div className="pipe-step">
            <div className="pipe-number">03</div>
            <div className="pipe-title">Render Deploy Hooks</div>
            <div className="pipe-desc">Triggers zero-downtime rolling update on Render Web Service & Background Worker</div>
          </div>
          <div className="pipe-arrow">&rarr;</div>
          <div className="pipe-step">
            <div className="pipe-number">04</div>
            <div className="pipe-title">Health Check Verification</div>
            <div className="pipe-desc">Validates /api/v1/health live probe before routing production traffic</div>
          </div>
        </div>
      </div>
    </div>
  );
};
