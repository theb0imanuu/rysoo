import { supabase } from './supabase';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export interface UserProfile {
  id: string;
  email: string;
  fullName: string | null;
  createdAt: string;
}

export interface JobStatusResponse {
  jobId: string;
  jobType: string;
  status: 'QUEUED' | 'PROCESSING' | 'COMPLETED' | 'FAILED' | 'RETRYING';
  attemptCount: number;
  maxRetries: number;
  idempotencyKey: string;
  resultJson: string | null;
  errorMessage: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface IngestionResult {
  rawText: string;
  normalizedMarkdown: string;
  detectedMimeType: string;
  contentHashSha256: string;
  ocrFallbackRequired: boolean;
  warnings: string[];
}

export interface OptimizationResult {
  tailoredMarkdown: string;
  claimedEmployers: string[];
  claimedDegrees: string[];
  claimedSkills: string[];
  claimedAccomplishments: string[];
  atsScore: number;
  keywordCoverageScore: number;
  factualCheckResult: {
    passed: boolean;
    violations: Array<{
      category: string;
      claimedFact: string;
      explanation: string;
    }>;
  };
  evidenceMapping: Array<{
    requirementText: string;
    substantiatedQuote: string | null;
    confidenceLevel: 'DEMONSTRATED' | 'PARTIALLY_SUPPORTED' | 'MISSING';
    matchScore: number;
  }>;
  structuralImprovements: string[];
}

async function getAuthHeader(): Promise<Record<string, string>> {
  const { data } = await supabase.auth.getSession();
  const token = data?.session?.access_token;
  const correlationId = 'web-' + crypto.randomUUID();

  return {
    'X-Correlation-ID': correlationId,
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  };
}

export async function fetchHealth(): Promise<{ status: string; service: string }> {
  const response = await fetch(`${API_BASE_URL}/api/v1/health`);
  const json = await response.json();
  return json.data;
}

export async function fetchProfile(): Promise<UserProfile> {
  const headers = await getAuthHeader();
  const response = await fetch(`${API_BASE_URL}/api/v1/profile`, { headers });
  if (!response.ok) {
    throw new Error(`Failed to fetch profile: ${response.status}`);
  }
  const json = await response.json();
  return json.data;
}

export async function enqueueOptimization(payload: {
  cvDocumentId: string;
  jobDescriptionId: string;
  idempotencyKey: string;
}): Promise<JobStatusResponse> {
  const headers = await getAuthHeader();
  const response = await fetch(`${API_BASE_URL}/api/v1/jobs/cv-optimization`, {
    method: 'POST',
    headers: {
      ...headers,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    const errorJson = await response.json().catch(() => ({}));
    throw new Error(errorJson?.error?.message || `Failed to enqueue job: ${response.status}`);
  }

  const json = await response.json();
  return json.data;
}

export async function pollJobStatus(jobId: string): Promise<JobStatusResponse> {
  const headers = await getAuthHeader();
  const response = await fetch(`${API_BASE_URL}/api/v1/jobs/${jobId}`, { headers });
  if (!response.ok) {
    throw new Error(`Failed to poll job status: ${response.status}`);
  }
  const json = await response.json();
  return json.data;
}
