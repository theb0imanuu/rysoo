import React, { useState } from 'react';
import { Columns, AlignLeft, Copy, Check, ShieldCheck } from 'lucide-react';

interface DiffViewerProps {
  originalMarkdown: string;
  tailoredMarkdown: string;
}

export const DiffViewer: React.FC<DiffViewerProps> = ({ originalMarkdown, tailoredMarkdown }) => {
  const [viewMode, setViewMode] = useState<'split' | 'unified'>('split');
  const [copied, setCopied] = useState(false);

  const handleCopyTailored = () => {
    navigator.clipboard.writeText(tailoredMarkdown);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  // Simple line diff computation for visual inspection
  const originalLines = originalMarkdown.split('\n');
  const tailoredLines = tailoredMarkdown.split('\n');

  return (
    <div className="diff-viewer-container glass-panel">
      <div className="diff-header">
        <div className="diff-title-wrapper">
          <h4>CV Content Comparison</h4>
          <span className="badge badge-success">
            <ShieldCheck size={13} />
            Ground-Truth Verified
          </span>
        </div>

        <div className="diff-actions">
          <div className="view-toggle-group">
            <button
              className={`toggle-btn ${viewMode === 'split' ? 'active' : ''}`}
              onClick={() => setViewMode('split')}
              title="Side-by-side comparison"
            >
              <Columns size={15} />
              <span>Split</span>
            </button>
            <button
              className={`toggle-btn ${viewMode === 'unified' ? 'active' : ''}`}
              onClick={() => setViewMode('unified')}
              title="Unified diff list"
            >
              <AlignLeft size={15} />
              <span>Unified</span>
            </button>
          </div>

          <button className="btn-secondary btn-sm" onClick={handleCopyTailored} title="Copy tailored markdown">
            {copied ? <Check size={14} className="text-success" /> : <Copy size={14} />}
            <span>{copied ? 'Copied' : 'Copy Optimized'}</span>
          </button>
        </div>
      </div>

      {viewMode === 'split' ? (
        <div className="diff-split-grid">
          <div className="diff-column">
            <div className="diff-column-header original">
              <span className="column-label">ORIGINAL SOURCE CV (Ground Truth)</span>
              <span className="line-count">{originalLines.length} lines</span>
            </div>
            <div className="diff-code-editor">
              <pre className="code-content">
                {originalLines.map((line, idx) => (
                  <div key={idx} className="code-line">
                    <span className="line-number">{idx + 1}</span>
                    <span className="line-text">{line || ' '}</span>
                  </div>
                ))}
              </pre>
            </div>
          </div>

          <div className="diff-column">
            <div className="diff-column-header tailored">
              <span className="column-label">TAILORED ATS CV (Grounded & Keyword Optimized)</span>
              <span className="line-count">{tailoredLines.length} lines</span>
            </div>
            <div className="diff-code-editor">
              <pre className="code-content">
                {tailoredLines.map((line, idx) => {
                  const isNewOrModified = !originalLines.includes(line) && line.trim().length > 0;
                  return (
                    <div
                      key={idx}
                      className={`code-line ${isNewOrModified ? 'diff-added' : ''}`}
                    >
                      <span className="line-number">{idx + 1}</span>
                      <span className="line-text">{line || ' '}</span>
                    </div>
                  );
                })}
              </pre>
            </div>
          </div>
        </div>
      ) : (
        <div className="diff-unified-view">
          <div className="diff-column-header">
            <span className="column-label">UNIFIED DIFF (Green: Tailored ATS Enhancements)</span>
          </div>
          <div className="diff-code-editor">
            <pre className="code-content">
              {tailoredLines.map((line, idx) => {
                const isNewOrModified = !originalLines.includes(line) && line.trim().length > 0;
                return (
                  <div
                    key={idx}
                    className={`code-line ${isNewOrModified ? 'diff-added' : ''}`}
                  >
                    <span className="line-prefix">{isNewOrModified ? '+' : ' '}</span>
                    <span className="line-number">{idx + 1}</span>
                    <span className="line-text">{line || ' '}</span>
                  </div>
                );
              })}
            </pre>
          </div>
        </div>
      )}
    </div>
  );
};
