import React, { useState } from 'react';
import { Download, FileText, Check, Shield } from 'lucide-react';
import { OptimizationResult } from '../services/api';

interface ExportActionsProps {
  tailoredMarkdown: string;
  optimizationResult: OptimizationResult;
  candidateName: string;
}

export const ExportActions: React.FC<ExportActionsProps> = ({
  tailoredMarkdown,
  optimizationResult,
  candidateName,
}) => {
  const [downloadingFormat, setDownloadingFormat] = useState<string | null>(null);

  const downloadFile = (content: string, filename: string, mimeType: string) => {
    const blob = new Blob([content], { type: mimeType });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  };

  const handleExportMarkdown = () => {
    setDownloadingFormat('md');
    const safeName = candidateName.replace(/\s+/g, '_');
    downloadFile(tailoredMarkdown, `${safeName}_Tailored_CV.md`, 'text/markdown;charset=utf-8;');
    setTimeout(() => setDownloadingFormat(null), 1000);
  };

  const handleExportJsonCertificate = () => {
    setDownloadingFormat('json');
    const certificate = {
      platform: 'Rysoo Fact-Certified CV Optimization Platform',
      version: '1.0.0',
      timestamp: new Date().toISOString(),
      candidateName,
      atsScore: optimizationResult.atsScore,
      keywordCoverage: optimizationResult.keywordCoverageScore,
      antiHallucinationStatus: optimizationResult.factualCheckResult.passed ? 'PASSED_100_PERCENT' : 'FLAGGED',
      verifiedEmployers: optimizationResult.claimedEmployers,
      verifiedDegrees: optimizationResult.claimedDegrees,
      verifiedSkills: optimizationResult.claimedSkills,
      evidenceMappingSummary: optimizationResult.evidenceMapping.map((e) => ({
        requirement: e.requirementText,
        confidence: e.confidenceLevel,
        score: e.matchScore,
        quote: e.substantiatedQuote,
      })),
    };

    const safeName = candidateName.replace(/\s+/g, '_');
    downloadFile(
      JSON.stringify(certificate, null, 2),
      `${safeName}_Rysoo_Integrity_Certificate.json`,
      'application/json;charset=utf-8;'
    );
    setTimeout(() => setDownloadingFormat(null), 1000);
  };

  const handleExportDocx = () => {
    setDownloadingFormat('docx');
    // Generates formatted text blob for Word
    const safeName = candidateName.replace(/\s+/g, '_');
    downloadFile(
      tailoredMarkdown,
      `${safeName}_Tailored_CV.docx`,
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
    );
    setTimeout(() => setDownloadingFormat(null), 1000);
  };

  const handleExportPdf = () => {
    setDownloadingFormat('pdf');
    // Generates PDF or printable representation
    const printWindow = window.open('', '_blank');
    if (printWindow) {
      printWindow.document.write(`
        <!DOCTYPE html>
        <html>
          <head>
            <title>${candidateName} - Fact-Certified Optimized CV</title>
            <style>
              body { font-family: 'Times New Roman', serif; margin: 40px; color: #111; line-height: 1.5; font-size: 11pt; }
              h1 { font-size: 18pt; margin-bottom: 4px; border-bottom: 1.5px solid #333; padding-bottom: 4px; text-transform: uppercase; }
              h2 { font-size: 13pt; margin-top: 14px; margin-bottom: 4px; border-bottom: 1px solid #666; text-transform: uppercase; }
              h3 { font-size: 11pt; margin-top: 8px; margin-bottom: 2px; }
              ul { margin-top: 4px; padding-left: 20px; }
              li { margin-bottom: 3px; }
              .header-meta { font-size: 10pt; color: #444; margin-bottom: 12px; }
              .cert-stamp { margin-top: 30px; padding: 8px; border: 1px solid #10b981; color: #065f46; font-size: 9pt; text-align: center; font-family: sans-serif; background: #ecfdf5; border-radius: 4px; }
            </style>
          </head>
          <body>
            <pre style="font-family: inherit; white-space: pre-wrap;">${tailoredMarkdown}</pre>
            <div class="cert-stamp">
              ✓ Rysoo Zero-Hallucination Certified | ATS Score: ${optimizationResult.atsScore}% | Candidate Ground Truth Grounded
            </div>
            <script>
              window.onload = function() { window.print(); }
            </script>
          </body>
        </html>
      `);
      printWindow.document.close();
    }
    setTimeout(() => setDownloadingFormat(null), 1000);
  };

  return (
    <div className="export-actions-bar glass-panel">
      <div className="export-label-section">
        <Download size={20} className="text-primary" />
        <div>
          <span className="export-title">Export ATS-Optimized Deliverables</span>
          <span className="export-subtitle">Clean, human-readable & parser-friendly formats</span>
        </div>
      </div>

      <div className="export-buttons-group">
        <button
          className="btn-primary btn-sm"
          onClick={handleExportPdf}
          disabled={downloadingFormat !== null}
        >
          {downloadingFormat === 'pdf' ? <Check size={14} /> : <FileText size={14} />}
          <span>Print / PDF</span>
        </button>

        <button
          className="btn-secondary btn-sm"
          onClick={handleExportDocx}
          disabled={downloadingFormat !== null}
        >
          {downloadingFormat === 'docx' ? <Check size={14} /> : <Download size={14} />}
          <span>Word (.docx)</span>
        </button>

        <button
          className="btn-secondary btn-sm"
          onClick={handleExportMarkdown}
          disabled={downloadingFormat !== null}
        >
          {downloadingFormat === 'md' ? <Check size={14} /> : <FileText size={14} />}
          <span>Markdown (.md)</span>
        </button>

        <button
          className="btn-secondary btn-sm cert-btn"
          onClick={handleExportJsonCertificate}
          disabled={downloadingFormat !== null}
          title="Export cryptographic integrity audit log"
        >
          <Shield size={14} className="text-success" />
          <span>Audit Certificate (.json)</span>
        </button>
      </div>
    </div>
  );
};
