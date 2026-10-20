import { describe, it, expect } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { EvidenceTable } from './EvidenceTable';
import { OptimizationResult } from '../services/api';

const mockEvidenceMapping: OptimizationResult['evidenceMapping'] = [
  {
    requirementText: 'High-throughput Java development',
    substantiatedQuote: 'Sustained 450,000 writes/sec in Java',
    confidenceLevel: 'DEMONSTRATED',
    matchScore: 0.98,
  },
  {
    requirementText: 'Kubernetes orchestration',
    substantiatedQuote: 'Migrated services to Kubernetes',
    confidenceLevel: 'PARTIALLY_SUPPORTED',
    matchScore: 0.75,
  },
  {
    requirementText: 'Machine Learning model training',
    substantiatedQuote: null,
    confidenceLevel: 'MISSING',
    matchScore: 0.0,
  },
];

describe('EvidenceTable', () => {
  it('renders all evidence items by default', () => {
    render(<EvidenceTable evidenceMapping={mockEvidenceMapping} />);
    expect(screen.getByText('High-throughput Java development')).toBeDefined();
    expect(screen.getByText('Kubernetes orchestration')).toBeDefined();
    expect(screen.getByText('Machine Learning model training')).toBeDefined();
    expect(screen.getByText('Demonstrated (1)')).toBeDefined();
    expect(screen.getByText('Partially Supported (1)')).toBeDefined();
    expect(screen.getByText('Missing (1)')).toBeDefined();
  });

  it('filters by DEMONSTRATED status', () => {
    render(<EvidenceTable evidenceMapping={mockEvidenceMapping} />);
    const filterBtn = screen.getByText('Demonstrated (1)');
    fireEvent.click(filterBtn);
    expect(screen.getByText('High-throughput Java development')).toBeDefined();
    expect(screen.queryByText('Machine Learning model training')).toBeNull();
  });

  it('filters by search query', () => {
    render(<EvidenceTable evidenceMapping={mockEvidenceMapping} />);
    const searchInput = screen.getByPlaceholderText(/Search requirements/);
    fireEvent.change(searchInput, { target: { value: 'Kubernetes' } });
    expect(screen.getByText('Kubernetes orchestration')).toBeDefined();
    expect(screen.queryByText('High-throughput Java development')).toBeNull();
  });
});
