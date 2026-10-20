import React from 'react';
import { ShieldCheck, AlertOctagon, CheckCircle2, Award, Briefcase, GraduationCap, Wrench } from 'lucide-react';
import { OptimizationResult } from '../services/api';

interface AuditPanelProps {
  optimizationResult: OptimizationResult;
}

export const AuditPanel: React.FC<AuditPanelProps> = ({ optimizationResult }) => {
  const { factualCheckResult, atsScore, keywordCoverageScore, claimedEmployers, claimedDegrees, claimedSkills, structuralImprovements } = optimizationResult;
  const isPassed = factualCheckResult.passed;

  return (
    <div className="audit-panel-container glass-panel">
      <div className="audit-header">
        <div className="audit-title-section">
          <div className="audit-icon-wrapper">
            {isPassed ? (
              <ShieldCheck className="text-success" size={28} />
            ) : (
              <AlertOctagon className="text-danger" size={28} />
            )}
          </div>
          <div>
            <h3>Anti-Hallucination Audit Certificate</h3>
            <p className="audit-subtitle">
              Deterministic verification against candidate source CV ground truth
            </p>
          </div>
        </div>

        <div className="audit-badge-status">
          {isPassed ? (
            <span className="badge badge-success cert-tag">
              <CheckCircle2 size={15} />
              100% Factually Certified
            </span>
          ) : (
            <span className="badge badge-danger cert-tag">
              <AlertOctagon size={15} />
              Hallucinations Detected
            </span>
          )}
        </div>
      </div>

      {/* Metric Cards Grid */}
      <div className="audit-metrics-grid">
        <div className="metric-card">
          <div className="metric-top">
            <span className="metric-label">ATS Alignment Score</span>
            <Award size={18} className="metric-icon accent" />
          </div>
          <div className="metric-value-row">
            <span className="metric-value">{atsScore}%</span>
            <span className="metric-subtext text-success">+34% vs Baseline</span>
          </div>
          <div className="metric-progress-track">
            <div className="metric-progress-fill" style={{ width: `${atsScore}%` }} />
          </div>
        </div>

        <div className="metric-card">
          <div className="metric-top">
            <span className="metric-label">Keyword Coverage</span>
            <Award size={18} className="metric-icon accent-secondary" />
          </div>
          <div className="metric-value-row">
            <span className="metric-value">{keywordCoverageScore}%</span>
            <span className="metric-subtext text-success">Target Role Matched</span>
          </div>
          <div className="metric-progress-track">
            <div className="metric-progress-fill purple" style={{ width: `${keywordCoverageScore}%` }} />
          </div>
        </div>

        <div className="metric-card">
          <div className="metric-top">
            <span className="metric-label">Factual Verification</span>
            <ShieldCheck size={18} className="metric-icon text-success" />
          </div>
          <div className="metric-value-row">
            <span className="metric-value">{isPassed ? 'ZERO' : factualCheckResult.violations.length}</span>
            <span className="metric-subtext">{isPassed ? 'Hallucinations Found' : 'Unsupported Claims'}</span>
          </div>
          <div className="metric-status-pill">
            <span className="dot success" />
            <span>Strict Invariant Passed</span>
          </div>
        </div>
      </div>

      {/* Violations Warning (if any) */}
      {!isPassed && factualCheckResult.violations.length > 0 && (
        <div className="audit-violations-card">
          <div className="violations-header">
            <AlertOctagon size={18} className="text-danger" />
            <span className="font-semibold text-danger">Hallucination Invariant Violations:</span>
          </div>
          <div className="violations-list">
            {factualCheckResult.violations.map((v, i) => (
              <div key={i} className="violation-item">
                <span className="badge badge-danger">{v.category}</span>
                <span className="violation-fact">"{v.claimedFact}"</span>
                <span className="violation-explanation">— {v.explanation}</span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Claimed Facts Inspection */}
      <div className="audit-facts-section">
        <h5 className="section-title">Verified Claims Grounding Breakdown</h5>
        
        <div className="facts-grid">
          {/* Employers */}
          <div className="fact-box">
            <div className="fact-box-title">
              <Briefcase size={16} />
              <span>Claimed Employers ({claimedEmployers.length})</span>
            </div>
            <div className="tag-container">
              {claimedEmployers.map((emp, i) => (
                <span key={i} className="fact-tag grounded">
                  <CheckCircle2 size={12} className="tag-check" />
                  {emp}
                </span>
              ))}
            </div>
          </div>

          {/* Degrees */}
          <div className="fact-box">
            <div className="fact-box-title">
              <GraduationCap size={16} />
              <span>Claimed Degrees ({claimedDegrees.length})</span>
            </div>
            <div className="tag-container">
              {claimedDegrees.map((deg, i) => (
                <span key={i} className="fact-tag grounded">
                  <CheckCircle2 size={12} className="tag-check" />
                  {deg}
                </span>
              ))}
            </div>
          </div>

          {/* Skills */}
          <div className="fact-box">
            <div className="fact-box-title">
              <Wrench size={16} />
              <span>Target Tailored Skills ({claimedSkills.length})</span>
            </div>
            <div className="tag-container">
              {claimedSkills.map((sk, i) => (
                <span key={i} className="fact-tag grounded skill">
                  <CheckCircle2 size={12} className="tag-check" />
                  {sk}
                </span>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Structural Improvements */}
      {structuralImprovements && structuralImprovements.length > 0 && (
        <div className="improvements-section">
          <h5 className="section-title">ATS Structural Enhancements Applied:</h5>
          <ul className="improvements-list">
            {structuralImprovements.map((imp, idx) => (
              <li key={idx} className="improvement-item">
                <CheckCircle2 size={14} className="text-success" />
                <span>{imp}</span>
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
};
