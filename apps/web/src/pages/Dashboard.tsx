import React from 'react';
import { ShieldCheck, Award, Zap, FileText, ArrowRight, CheckCircle2, Server, Database } from 'lucide-react';
import { IngestedCvPayload } from '../components/UploadModal';

interface DashboardProps {
  onOpenUpload: () => void;
  onLaunchStudioWithCv: (cv: IngestedCvPayload) => void;
  onOpenArchitecture: () => void;
}

const DEFAULT_DOCUMENTS: IngestedCvPayload[] = [
  {
    id: '00000000-0000-0000-0000-000000000001',
    title: 'Jane Doe - Senior Staff Distributed Systems Engineer',
    fileName: 'Jane_Doe_CV.pdf',
    markdown: `# Jane Doe\nSenior Staff Distributed Systems Engineer\n\n## Experience\n- Acme Cloud Technologies (2020-Present)\n- DataScale Corp (2016-2020)`,
    employers: ['Acme Cloud Technologies', 'DataScale Corp'],
    degrees: ['M.S. Computer Science (Stanford)', 'B.S. Software Engineering (UC Berkeley)'],
    skills: ['Java', 'Go', 'Spring Boot', 'PostgreSQL', 'Docker', 'Kubernetes', 'AWS', 'Kafka'],
  },
  {
    id: '00000000-0000-0000-0000-000000000002',
    title: 'Alex Chen - Principal Platform & DevOps Engineer',
    fileName: 'Alex_Chen_CV.docx',
    markdown: `# Alex Chen\nPrincipal DevOps & SRE Engineer\n\n## Experience\n- Nexus Cloud Systems (2021-Present)\n- Stellar Payments (2017-2021)`,
    employers: ['Nexus Cloud Systems', 'Stellar Payments'],
    degrees: ['B.S. Computer Engineering (Univ. of Washington)'],
    skills: ['Docker', 'Kubernetes', 'Terraform', 'AWS', 'Render', 'Python', 'Go', 'CI/CD'],
  },
];

export const Dashboard: React.FC<DashboardProps> = ({
  onOpenUpload,
  onLaunchStudioWithCv,
  onOpenArchitecture,
}) => {
  return (
    <div className="dashboard-page">
      {/* Hero section */}
      <section className="dashboard-hero glass-panel">
        <div className="hero-content">
          <div className="hero-badge">
            <ShieldCheck size={16} className="text-success" />
            <span>ENTERPRISE FACT-CERTIFIED ATS OPTIMIZATION</span>
          </div>
          <h1 className="hero-title">
            Tailor Your CV With <span className="gradient-text">Zero Hallucinations</span>
          </h1>
          <p className="hero-subtitle">
            Rysoo's deterministic engine verifies that every single claimed employer, degree, and skill is strictly grounded in candidate ground-truth facts while maximizing ATS match scoring.
          </p>
          <div className="hero-actions">
            <button
              className="btn-primary"
              onClick={() => onLaunchStudioWithCv(DEFAULT_DOCUMENTS[0])}
            >
              <span>Launch Studio (Jane Doe Demo)</span>
              <ArrowRight size={16} />
            </button>
            <button className="btn-secondary" onClick={onOpenUpload}>
              <FileText size={16} />
              <span>Ingest New Document</span>
            </button>
          </div>
        </div>

        <div className="hero-card-preview">
          <div className="hero-cert-card">
            <div className="cert-card-header">
              <ShieldCheck size={24} className="text-success" />
              <span>Deterministic Anti-Hallucination Gate</span>
            </div>
            <div className="cert-stats">
              <div className="cert-stat-item">
                <span className="stat-label">Claimed Employers Grounding</span>
                <span className="stat-val text-success">100% Grounded</span>
              </div>
              <div className="cert-stat-item">
                <span className="stat-label">Claimed Degrees Grounding</span>
                <span className="stat-val text-success">100% Grounded</span>
              </div>
              <div className="cert-stat-item">
                <span className="stat-label">Invented Skills Detected</span>
                <span className="stat-val text-success">0 (Strictly Blocked)</span>
              </div>
            </div>
            <div className="cert-footer">
              <CheckCircle2 size={14} className="text-success" />
              <span>Verified against Spring Boot Domain Invariant Engine</span>
            </div>
          </div>
        </div>
      </section>

      {/* Metric stats row */}
      <section className="stats-grid">
        <div className="stat-card glass-panel">
          <div className="stat-icon-wrapper success">
            <ShieldCheck size={22} />
          </div>
          <div className="stat-info">
            <span className="stat-number">100%</span>
            <span className="stat-name">Zero-Hallucination Rate</span>
            <span className="stat-desc">Deterministic factual verification</span>
          </div>
        </div>

        <div className="stat-card glass-panel">
          <div className="stat-icon-wrapper primary">
            <Award size={22} />
          </div>
          <div className="stat-info">
            <span className="stat-number">+38%</span>
            <span className="stat-name">Average ATS Score Boost</span>
            <span className="stat-desc">Aligned keywords & structure</span>
          </div>
        </div>

        <div className="stat-card glass-panel">
          <div className="stat-icon-wrapper purple">
            <Zap size={22} />
          </div>
          <div className="stat-info">
            <span className="stat-number">&lt; 850ms</span>
            <span className="stat-name">Queue Processing Latency</span>
            <span className="stat-desc">Durable worker polling</span>
          </div>
        </div>

        <div className="stat-card glass-panel clickable" onClick={onOpenArchitecture}>
          <div className="stat-icon-wrapper amber">
            <Server size={22} />
          </div>
          <div className="stat-info">
            <span className="stat-number">Option A</span>
            <span className="stat-name">Spring Boot Exclusive API</span>
            <span className="stat-desc">Zero direct Supabase DB access</span>
          </div>
        </div>
      </section>

      {/* Available CV Documents */}
      <section className="documents-section glass-panel">
        <div className="section-header">
          <div>
            <h3>Candidate Verified Documents</h3>
            <p className="section-subtitle">
              Ground-truth profiles available for immediate job description targeting
            </p>
          </div>
          <button className="btn-secondary btn-sm" onClick={onOpenUpload}>
            + Ingest Another CV
          </button>
        </div>

        <div className="documents-list">
          {DEFAULT_DOCUMENTS.map((doc) => (
            <div key={doc.id} className="doc-row-item">
              <div className="doc-left">
                <div className="doc-icon-wrapper">
                  <FileText size={20} />
                </div>
                <div>
                  <h4 className="doc-title">{doc.title}</h4>
                  <div className="doc-meta">
                    <span>{doc.fileName}</span>
                    <span>•</span>
                    <span>{doc.employers.length} Employers</span>
                    <span>•</span>
                    <span>{doc.skills.length} Grounded Skills</span>
                    <span>•</span>
                    <span className="text-success">Verified Ground Truth</span>
                  </div>
                </div>
              </div>

              <div className="doc-right">
                <button
                  className="btn-primary btn-sm"
                  onClick={() => onLaunchStudioWithCv(doc)}
                >
                  <span>Open in Studio</span>
                  <ArrowRight size={14} />
                </button>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* Architecture Option A Banner */}
      <section className="architecture-summary-banner glass-panel" onClick={onOpenArchitecture} role="button" tabIndex={0}>
        <div className="arch-banner-left">
          <Database size={28} className="text-primary" />
          <div>
            <h4>Option A Architecture Enforcement</h4>
            <p>
              Web and mobile clients authenticate using Supabase Auth (JWT) and communicate exclusively with Spring Boot REST endpoints (<code className="code-pill">/api/v1/*</code>). Direct client querying of Supabase database tables is strictly prohibited.
            </p>
          </div>
        </div>
        <div className="arch-banner-right">
          <span className="btn-secondary btn-sm">Inspect Full Architecture &rarr;</span>
        </div>
      </section>
    </div>
  );
};
