import React, { useState } from 'react';
import { CheckCircle2, AlertCircle, HelpCircle, Search } from 'lucide-react';
import { OptimizationResult } from '../services/api';

interface EvidenceTableProps {
  evidenceMapping: OptimizationResult['evidenceMapping'];
}

export const EvidenceTable: React.FC<EvidenceTableProps> = ({ evidenceMapping }) => {
  const [filterLevel, setFilterLevel] = useState<'ALL' | 'DEMONSTRATED' | 'PARTIALLY_SUPPORTED' | 'MISSING'>('ALL');
  const [searchQuery, setSearchQuery] = useState('');

  const filteredItems = evidenceMapping.filter((item) => {
    const matchesFilter = filterLevel === 'ALL' || item.confidenceLevel === filterLevel;
    const matchesSearch =
      item.requirementText.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (item.substantiatedQuote && item.substantiatedQuote.toLowerCase().includes(searchQuery.toLowerCase()));
    return matchesFilter && matchesSearch;
  });

  const demonstratedCount = evidenceMapping.filter((i) => i.confidenceLevel === 'DEMONSTRATED').length;
  const partialCount = evidenceMapping.filter((i) => i.confidenceLevel === 'PARTIALLY_SUPPORTED').length;
  const missingCount = evidenceMapping.filter((i) => i.confidenceLevel === 'MISSING').length;

  return (
    <div className="evidence-table-container glass-panel">
      <div className="table-header-row">
        <div>
          <h4>Requirement Grounding Matrix</h4>
          <p className="table-subtitle">
            Every job requirement substantiated with direct source CV evidence quotes
          </p>
        </div>

        {/* Filter Pills */}
        <div className="filter-pills-row">
          <button
            className={`filter-pill ${filterLevel === 'ALL' ? 'active' : ''}`}
            onClick={() => setFilterLevel('ALL')}
          >
            All ({evidenceMapping.length})
          </button>
          <button
            className={`filter-pill success ${filterLevel === 'DEMONSTRATED' ? 'active' : ''}`}
            onClick={() => setFilterLevel('DEMONSTRATED')}
          >
            Demonstrated ({demonstratedCount})
          </button>
          <button
            className={`filter-pill warning ${filterLevel === 'PARTIALLY_SUPPORTED' ? 'active' : ''}`}
            onClick={() => setFilterLevel('PARTIALLY_SUPPORTED')}
          >
            Partially Supported ({partialCount})
          </button>
          <button
            className={`filter-pill danger ${filterLevel === 'MISSING' ? 'active' : ''}`}
            onClick={() => setFilterLevel('MISSING')}
          >
            Missing ({missingCount})
          </button>
        </div>
      </div>

      {/* Search Bar */}
      <div className="table-search-bar">
        <Search size={16} className="search-icon" />
        <input
          type="text"
          placeholder="Search requirements or substantiated evidence quotes..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
        />
      </div>

      {/* Table */}
      <div className="table-scroll-wrapper">
        <table className="evidence-table">
          <thead>
            <tr>
              <th style={{ width: '40%' }}>Job Description Requirement</th>
              <th style={{ width: '18%' }}>Match Level</th>
              <th style={{ width: '42%' }}>Candidate Source Evidence Quote</th>
            </tr>
          </thead>
          <tbody>
            {filteredItems.length === 0 ? (
              <tr>
                <td colSpan={3} className="empty-row">
                  No requirements match the selected filter.
                </td>
              </tr>
            ) : (
              filteredItems.map((item, idx) => (
                <tr key={idx} className={`evidence-row level-${item.confidenceLevel.toLowerCase()}`}>
                  <td className="requirement-cell">
                    <span className="requirement-text">{item.requirementText}</span>
                  </td>
                  <td className="status-cell">
                    {item.confidenceLevel === 'DEMONSTRATED' && (
                      <span className="badge badge-success">
                        <CheckCircle2 size={13} />
                        Demonstrated
                      </span>
                    )}
                    {item.confidenceLevel === 'PARTIALLY_SUPPORTED' && (
                      <span className="badge badge-warning">
                        <AlertCircle size={13} />
                        Partial Match
                      </span>
                    )}
                    {item.confidenceLevel === 'MISSING' && (
                      <span className="badge badge-danger">
                        <HelpCircle size={13} />
                        Missing Fact
                      </span>
                    )}
                    <span className="score-percentage">{(item.matchScore * 100).toFixed(0)}%</span>
                  </td>
                  <td className="evidence-cell">
                    {item.substantiatedQuote ? (
                      <blockquote className="quote-box">
                        <span className="quote-mark">“</span>
                        {item.substantiatedQuote}
                        <span className="quote-mark">”</span>
                      </blockquote>
                    ) : (
                      <span className="unsubstantiated-label">
                        No supporting claim found in source CV (Hallucination protection active)
                      </span>
                    )}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};
