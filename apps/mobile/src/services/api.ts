import { supabase } from './supabase';

const API_BASE_URL = process.env.EXPO_PUBLIC_API_BASE_URL || 'http://localhost:8080';

export interface MobileJobStatus {
  jobId: string;
  status: 'QUEUED' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
  resultJson: string | null;
  errorMessage: string | null;
}

export interface MobileOptimizationResult {
  atsScore: number;
  keywordCoverageScore: number;
  tailoredMarkdown: string;
  claimedEmployers: string[];
  claimedDegrees: string[];
  claimedSkills: string[];
  factualPassed: boolean;
}

async function getAuthHeaders(): Promise<Record<string, string>> {
  const { data } = await supabase.auth.getSession();
  const token = data?.session?.access_token;
  const correlationId = 'mobile-' + Date.now();

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

export async function enqueueMobileOptimization(payload: {
  cvDocumentId: string;
  jobDescriptionId: string;
  idempotencyKey: string;
}): Promise<MobileJobStatus> {
  const headers = await getAuthHeaders();
  const response = await fetch(`${API_BASE_URL}/api/v1/jobs/cv-optimization`, {
    method: 'POST',
    headers: {
      ...headers,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    throw new Error(`Failed to enqueue job: ${response.status}`);
  }

  const json = await response.json();
  return json.data;
}

export async function pollMobileJobStatus(jobId: string): Promise<MobileJobStatus> {
  const headers = await getAuthHeaders();
  const response = await fetch(`${API_BASE_URL}/api/v1/jobs/${jobId}`, { headers });
  if (!response.ok) {
    throw new Error(`Failed to poll status: ${response.status}`);
  }
  const json = await response.json();
  return json.data;
}
