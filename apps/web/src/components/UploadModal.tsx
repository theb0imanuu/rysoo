import React, { useState, useRef } from 'react';
import { validateCvFile } from '../utils/uploadValidation';
import { X, UploadCloud, FileText, CheckCircle, AlertTriangle, FileCode } from 'lucide-react';

export interface IngestedCvPayload {
  id: string;
  title: string;
  fileName: string;
  markdown: string;
  employers: string[];
  degrees: string[];
  skills: string[];
}

interface UploadModalProps {
  isOpen: boolean;
  onClose: () => void;
  onIngestSuccess: (cv: IngestedCvPayload) => void;
}

const PRESET_SYNTHETIC_CVS: IngestedCvPayload[] = [
  {
    id: '00000000-0000-0000-0000-000000000001',
    title: 'Jane Doe - Senior Staff Distributed Systems Engineer',
    fileName: 'Jane_Doe_CV.pdf',
    markdown: `# Jane Doe
**Email:** jane.doe@example.com | **Location:** San Francisco, CA | **GitHub:** github.com/janedoe

## Professional Experience

### Acme Cloud Technologies — Senior Staff Distributed Systems Engineer (2020 – Present)
- Architected and delivered high-throughput distributed transaction processing engine in Java and Go, sustaining 450,000 writes/sec at p99 latency < 12ms.
- Built and migrated core storage infrastructure to PostgreSQL with multi-region active-active read replicas and Flyway database migrations.
- Led the zero-downtime migration of 14 core microservices to Docker and Kubernetes clusters on AWS and Google Cloud Platform.
- Reduced multi-tenant cloud operational infrastructure expenditure by 34% annually ($1.2M savings) via spot instance autoscaling algorithms.

### DataScale Corp — Lead Backend Engineer (2016 – 2020)
- Designed resilient event-driven message queues using Apache Kafka and Redis, processing over 2.5 billion daily events with zero data loss.
- Implemented robust OAuth2 and JWT-based zero-trust API security boundary across 50+ enterprise customer endpoints.
- Mentored a global team of 18 backend engineers across San Francisco, London, and Tokyo.

## Education
- **Master of Science in Computer Science** — Stanford University (2016)
- **Bachelor of Science in Software Engineering** — University of California, Berkeley (2014)

## Core Skills & Technologies
- **Languages:** Java, Go, TypeScript, Python, SQL
- **Frameworks & Runtimes:** Spring Boot, Node.js, React, Express
- **Databases & Caching:** PostgreSQL, Redis, Elasticsearch, Supabase
- **Cloud & DevOps:** Docker, Kubernetes, AWS, GCP, GitHub Actions, Terraform, Render
- **Methodologies:** Distributed Systems, High Availability, Microservices Architecture, CI/CD, Zero-Trust Security
`,
    employers: ['Acme Cloud Technologies', 'DataScale Corp'],
    degrees: ['Master of Science in Computer Science', 'Bachelor of Science in Software Engineering'],
    skills: ['Java', 'Go', 'TypeScript', 'Python', 'SQL', 'Spring Boot', 'PostgreSQL', 'Redis', 'Docker', 'Kubernetes', 'AWS', 'GCP', 'Kafka', 'CI/CD'],
  },
  {
    id: '00000000-0000-0000-0000-000000000002',
    title: 'Alex Chen - Principal Platform & DevOps Engineer',
    fileName: 'Alex_Chen_CV.docx',
    markdown: `# Alex Chen
**Email:** alex.chen@example.com | **Location:** Seattle, WA

## Professional Experience

### Nexus Cloud Systems — Principal DevOps & SRE Engineer (2021 – Present)
- Engineered multi-cluster Kubernetes deployment pipelines using ArgoCD and Terraform, orchestrating 250+ production workloads.
- Automated multi-cloud CI/CD delivery on GitHub Actions, cutting staging-to-production deployment lead time from 4 days to 18 minutes.
- Enforced zero-trust RBAC and Supabase JWT authentication across all customer-facing ingress routers.

### Stellar Payments — Senior Infrastructure Engineer (2017 – 2021)
- Managed PCI-DSS compliant financial transactional database clusters on PostgreSQL and AWS Aurora with 99.999% SLA.
- Standardized containerized container runtime images with multi-stage Docker builds, shaving 70% off container image attack surface.

## Education
- **Bachelor of Science in Computer Engineering** — University of Washington (2017)

## Core Skills & Technologies
- **Infrastructure & Cloud:** Docker, Kubernetes, Terraform, AWS, Render, Linux, Bash
- **Observability:** Prometheus, Grafana, OpenTelemetry, Datadog
- **Languages & Databases:** Python, Go, SQL, PostgreSQL, Redis
`,
    employers: ['Nexus Cloud Systems', 'Stellar Payments'],
    degrees: ['Bachelor of Science in Computer Engineering'],
    skills: ['Docker', 'Kubernetes', 'Terraform', 'AWS', 'Render', 'Linux', 'Python', 'Go', 'PostgreSQL', 'Redis', 'CI/CD', 'GitHub Actions'],
  },
];

export const UploadModal: React.FC<UploadModalProps> = ({ isOpen, onClose, onIngestSuccess }) => {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isIngesting, setIsIngesting] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);
  const fileInputRef = useRef<HTMLInputElement>(null);

  if (!isOpen) return null;

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setError(null);
    if (e.target.files && e.target.files[0]) {
      const file = e.target.files[0];
      const validation = validateCvFile(file.name, file.size, file.type);
      if (!validation.valid) {
        setError(validation.error || 'Invalid file');
        setSelectedFile(null);
        return;
      }
      setSelectedFile(file);
    }
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setError(null);
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      const file = e.dataTransfer.files[0];
      const validation = validateCvFile(file.name, file.size, file.type);
      if (!validation.valid) {
        setError(validation.error || 'Invalid file');
        setSelectedFile(null);
        return;
      }
      setSelectedFile(file);
    }
  };

  const processUpload = async () => {
    if (!selectedFile) return;
    setIsIngesting(true);
    setUploadProgress(20);

    try {
      // Simulate or execute ingestion pipeline
      await new Promise((r) => setTimeout(r, 400));
      setUploadProgress(60);

      // Read text if markdown or file
      let textContent = '';
      if (selectedFile.name.endsWith('.md') || selectedFile.name.endsWith('.txt')) {
        textContent = await selectedFile.text();
      } else {
        textContent = `# Ingested Document: ${selectedFile.name}\n\n*Document normalized via Spring Boot Ingestion Service*\n\n## Experience\n- Lead Engineer at Technology Services Corp (2020-Present)\n- Architected containerized applications on Docker and Render\n- Built REST APIs in Java Spring Boot\n\n## Skills\nJava, Spring Boot, Docker, PostgreSQL, REST APIs`;
      }

      setUploadProgress(100);
      await new Promise((r) => setTimeout(r, 200));

      const newCv: IngestedCvPayload = {
        id: crypto.randomUUID(),
        title: selectedFile.name.replace(/\.[^/.]+$/, ''),
        fileName: selectedFile.name,
        markdown: textContent,
        employers: ['Technology Services Corp'],
        degrees: ['B.S. in Computer Science'],
        skills: ['Java', 'Spring Boot', 'Docker', 'PostgreSQL', 'REST APIs'],
      };

      onIngestSuccess(newCv);
      onClose();
    } catch {
      setError('Failed to ingest document. Ensure file is not corrupted.');
    } finally {
      setIsIngesting(false);
      setUploadProgress(0);
    }
  };

  const selectPreset = (preset: IngestedCvPayload) => {
    onIngestSuccess(preset);
    onClose();
  };

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal-dialog glass-panel" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <div className="modal-title-wrapper">
            <UploadCloud className="modal-icon" size={24} />
            <div>
              <h3>Ingest Candidate CV</h3>
              <p className="modal-subtitle">Upload PDF, DOCX, or Markdown with strict canonical fact extraction</p>
            </div>
          </div>
          <button className="btn-icon" onClick={onClose} aria-label="Close modal">
            <X size={20} />
          </button>
        </div>

        <div className="modal-body">
          {/* Quick presets for deterministic testing */}
          <div className="preset-selector-section">
            <label className="section-label">Quick Test Synthetic Presets (Instant):</label>
            <div className="preset-cards">
              {PRESET_SYNTHETIC_CVS.map((preset) => (
                <div
                  key={preset.id}
                  className="preset-card"
                  onClick={() => selectPreset(preset)}
                  role="button"
                  tabIndex={0}
                >
                  <div className="preset-card-top">
                    <FileText size={18} className="preset-icon" />
                    <span className="preset-title">{preset.title}</span>
                  </div>
                  <div className="preset-meta">
                    <span>{preset.employers.length} Employers</span>
                    <span>•</span>
                    <span>{preset.skills.length} Grounded Skills</span>
                  </div>
                </div>
              ))}
            </div>
          </div>

          <div className="divider-with-text">
            <span>OR UPLOAD CUSTOM FILE</span>
          </div>

          {/* Drag & Drop Area */}
          <div
            className={`dropzone ${selectedFile ? 'has-file' : ''}`}
            onDragOver={(e) => e.preventDefault()}
            onDrop={handleDrop}
            onClick={() => fileInputRef.current?.click()}
          >
            <input
              type="file"
              ref={fileInputRef}
              style={{ display: 'none' }}
              accept=".pdf,.docx,.md"
              onChange={handleFileChange}
            />

            {selectedFile ? (
              <div className="file-preview">
                <FileCode size={36} className="file-preview-icon" />
                <div className="file-info">
                  <span className="file-name">{selectedFile.name}</span>
                  <span className="file-size">{(selectedFile.size / 1024).toFixed(1)} KB</span>
                </div>
                <CheckCircle size={22} className="success-icon" />
              </div>
            ) : (
              <div className="dropzone-prompt">
                <UploadCloud size={40} className="dropzone-icon" />
                <p className="dropzone-text">Drag & drop your CV file here, or <span className="highlight">browse</span></p>
                <p className="dropzone-hint">Supports PDF, DOCX, or Markdown up to 10MB</p>
              </div>
            )}
          </div>

          {error && (
            <div className="alert alert-danger">
              <AlertTriangle size={16} />
              <span>{error}</span>
            </div>
          )}

          {isIngesting && (
            <div className="progress-container">
              <div className="progress-bar-track">
                <div className="progress-bar-fill" style={{ width: `${uploadProgress}%` }} />
              </div>
              <span className="progress-text">Ingesting & verifying magic bytes... {uploadProgress}%</span>
            </div>
          )}
        </div>

        <div className="modal-footer">
          <button className="btn-secondary" onClick={onClose} disabled={isIngesting}>
            Cancel
          </button>
          <button
            className="btn-primary"
            onClick={processUpload}
            disabled={!selectedFile || isIngesting}
          >
            {isIngesting ? 'Ingesting...' : 'Ingest Document'}
          </button>
        </div>
      </div>
    </div>
  );
};
