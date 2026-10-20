import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { AuditPanel } from './AuditPanel';
import { OptimizationResult } from '../services/api';

const mockSuccessResult: OptimizationResult = {
  tailoredMarkdown: '# Tailored CV',
  claimedEmployers: ['Acme Corp', 'DataScale'],
  claimedDegrees: ['M.S. Computer Science'],
  claimedSkills: ['Java', 'Spring Boot', 'Docker'],
  claimedAccomplishments: ['Built scalable microservices'],
  atsScore: 94,
  keywordCoverageScore: 92,
  factualCheckResult: {
    passed: true,
    violations: [],
  },
  evidenceMapping: [
    {
      requirementText: 'Java experience',
      substantiatedQuote: 'Engineered backend in Java',
      confidenceLevel: 'DEMONSTRATED',
      matchScore: 0.95,
    },
  ],
  structuralImprovements: ['Added quantitative metrics'],
};

const mockViolationResult: OptimizationResult = {
  ...mockSuccessResult,
  factualCheckResult: {
    passed: false,
    violations: [
      {
        category: 'EMPLOYER_HALLUCINATION',
        claimedFact: 'Google',
        explanation: 'Candidate never worked at Google',
      },
    ],
  },
};

describe('AuditPanel', () => {
  it('renders certified badge when anti-hallucination check passes', () => {
    render(<AuditPanel optimizationResult={mockSuccessResult} />);
    expect(screen.getByText('100% Factually Certified')).toBeDefined();
    expect(screen.getByText('94%')).toBeDefined();
    expect(screen.getByText('ZERO')).toBeDefined();
    expect(screen.getByText('Acme Corp')).toBeDefined();
  });

  it('renders violations warning when hallucination is detected', () => {
    render(<AuditPanel optimizationResult={mockViolationResult} />);
    expect(screen.getByText('Hallucinations Detected')).toBeDefined();
    expect(screen.getByText('EMPLOYER_HALLUCINATION')).toBeDefined();
    expect(screen.getByText(/Candidate never worked at Google/)).toBeDefined();
  });
});
