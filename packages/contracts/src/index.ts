// Common API Response Envelope
export interface ApiResponse<T> {
  success: boolean;
  data: T | null;
  error: ApiError | null;
  meta: {
    correlationId: string;
    timestamp: string;
  };
}

export interface ApiError {
  code: string;
  message: string;
  details?: Record<string, unknown>[];
}

// User Profile
export interface Profile {
  id: string;
  email: string;
  fullName: string | null;
  createdAt: string;
}

// CV Document & Versions
export type VersionType = 'ORIGINAL_UPLOAD' | 'NORMALIZED_MARKDOWN' | 'MANUALLY_EDITED' | 'OPTIMIZED';

export interface CVVersion {
  id: string;
  cvDocumentId: string;
  versionNumber: number;
  versionType: VersionType;
  fileName: string | null;
  fileSizeBytes: number | null;
  mimeType: string | null;
  rawText: string | null;
  normalizedMarkdown: string;
  structuredAstJson?: Record<string, unknown>;
  createdAt: string;
}

export interface CVDocument {
  id: string;
  userId: string;
  title: string;
  currentVersion: CVVersion | null;
  createdAt: string;
  updatedAt: string;
}

// Upload Intent
export interface UploadIntentRequest {
  fileName: string;
  mimeType: string;
  fileSizeBytes: number;
}

export interface UploadIntentResponse {
  cvDocumentId: string;
  cvVersionId: string;
  uploadUrl: string;
  storagePath: string;
  expiresInSeconds: number;
}

// Job Descriptions
export interface JobDescription {
  id: string;
  userId: string;
  targetRole: string;
  targetCompany: string | null;
  rawText: string;
  parsedRequirementsJson?: Record<string, unknown>;
  createdAt: string;
  updatedAt: string;
}

export interface CreateJobDescriptionRequest {
  targetRole: string;
  targetCompany?: string;
  rawText: string;
}

// Optimization
export type OptimizationStatus = 'QUEUED' | 'PROCESSING' | 'COMPLETED' | 'FAILED' | 'CANCELLED';

export interface RequirementEvidence {
  requirement: string;
  evidence: string | null;
  confidenceScore: number;
}

export interface EvidenceMapping {
  demonstrated: RequirementEvidence[];
  partiallySupported: RequirementEvidence[];
  missing: RequirementEvidence[];
}

export interface OptimizationResult {
  id: string;
  optimizationRunId: string;
  tailoredMarkdown: string;
  evidenceMapping: EvidenceMapping;
  matchScorePercentage: number;
  matchExplanationMarkdown: string;
  factualVerificationStatus: 'PASSED' | 'FLAGGED_INCONSISTENCIES';
  verificationReportJson?: Record<string, unknown>;
  diffSummaryJson?: Record<string, unknown>;
  createdAt: string;
}

export interface OptimizationRun {
  id: string;
  projectId: string;
  sourceCvVersionId: string;
  jobDescriptionId: string;
  aiProvider: string;
  aiModelIdentifier: string;
  status: OptimizationStatus;
  attemptCount: number;
  estimatedCostUsd: number;
  result: OptimizationResult | null;
  startedAt: string | null;
  completedAt: string | null;
  createdAt: string;
}

export interface StartOptimizationRequest {
  cvVersionId: string;
  jobDescriptionId: string;
  projectName: string;
}

// Exports
export type ExportFormat = 'PDF' | 'DOCX' | 'MARKDOWN';

export interface ExportRequest {
  optimizationResultId: string;
  format: ExportFormat;
}

export interface ExportFile {
  id: string;
  optimizationResultId: string;
  format: ExportFormat;
  fileName: string;
  fileSizeBytes: number;
  mimeType: string;
  createdAt: string;
}

export interface DownloadUrlResponse {
  downloadUrl: string;
  expiresInSeconds: number;
}
