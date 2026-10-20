import React, { useState } from 'react';
import { Navbar } from './components/Navbar';
import { UploadModal, IngestedCvPayload } from './components/UploadModal';
import { Dashboard } from './pages/Dashboard';
import { OptimizationStudio } from './pages/OptimizationStudio';
import { Architecture } from './pages/Architecture';
import { ShieldCheck, GitBranch } from 'lucide-react';

const INITIAL_DEMO_CV: IngestedCvPayload = {
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
`,
  employers: ['Acme Cloud Technologies', 'DataScale Corp'],
  degrees: ['Master of Science in Computer Science', 'Bachelor of Science in Software Engineering'],
  skills: ['Java', 'Go', 'TypeScript', 'Python', 'SQL', 'Spring Boot', 'PostgreSQL', 'Redis', 'Docker', 'Kubernetes', 'AWS', 'GCP', 'Kafka', 'CI/CD'],
};

export const App: React.FC = () => {
  const [currentTab, setCurrentTab] = useState<'dashboard' | 'studio' | 'architecture'>('dashboard');
  const [isUploadOpen, setIsUploadOpen] = useState(false);
  const [selectedCv, setSelectedCv] = useState<IngestedCvPayload | null>(INITIAL_DEMO_CV);

  const handleLaunchStudioWithCv = (cv: IngestedCvPayload) => {
    setSelectedCv(cv);
    setCurrentTab('studio');
  };

  const handleIngestSuccess = (cv: IngestedCvPayload) => {
    setSelectedCv(cv);
    setCurrentTab('studio');
  };

  return (
    <div className="app-root">
      <Navbar
        currentTab={currentTab}
        onSelectTab={setCurrentTab}
        onOpenUpload={() => setIsUploadOpen(true)}
      />

      <main className="main-content">
        {currentTab === 'dashboard' && (
          <Dashboard
            onOpenUpload={() => setIsUploadOpen(true)}
            onLaunchStudioWithCv={handleLaunchStudioWithCv}
            onOpenArchitecture={() => setCurrentTab('architecture')}
          />
        )}

        {currentTab === 'studio' && (
          <OptimizationStudio
            initialCv={selectedCv}
            onOpenUpload={() => setIsUploadOpen(true)}
          />
        )}

        {currentTab === 'architecture' && <Architecture />}
      </main>

      <UploadModal
        isOpen={isUploadOpen}
        onClose={() => setIsUploadOpen(false)}
        onIngestSuccess={handleIngestSuccess}
      />

      <footer className="app-footer">
        <div className="footer-content">
          <div className="footer-left">
            <span className="footer-brand">RYSOO</span>
            <span className="footer-tagline">Fact-Certified CV Optimization Platform</span>
            <span className="bullet">•</span>
            <span className="footer-arch">Option A Spring Boot Protected Architecture</span>
          </div>

          <div className="footer-right">
            <div className="footer-cert">
              <ShieldCheck size={14} className="text-success" />
              <span>Zero-Hallucination Certified</span>
            </div>
            <a
              href="https://github.com/theb0imanuu/rysoo.git"
              target="_blank"
              rel="noopener noreferrer"
              className="footer-repo-link"
            >
              <GitBranch size={14} />
              <span>GitHub: theb0imanuu/rysoo</span>
            </a>
          </div>
        </div>
      </footer>
    </div>
  );
};
export default App;
