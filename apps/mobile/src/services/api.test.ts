import { describe, it, expect, vi, beforeEach } from 'vitest';
import { fetchHealth, enqueueMobileOptimization, pollMobileJobStatus } from './api';

describe('Mobile API Service (Option A Rule Enforcement)', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('fetchHealth queries Spring Boot /api/v1/health', async () => {
    const mockHealthResponse = {
      status: 'UP',
      service: 'rysoo-api',
    };

    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => ({ success: true, data: mockHealthResponse }),
    } as Response);

    const result = await fetchHealth();
    expect(result.status).toBe('UP');
    expect(result.service).toBe('rysoo-api');
    expect(global.fetch).toHaveBeenCalledWith('http://localhost:8080/api/v1/health');
  });

  it('enqueueMobileOptimization sends POST with correlation id header', async () => {
    const mockJobResponse = {
      jobId: 'job-123',
      status: 'QUEUED',
      resultJson: null,
      errorMessage: null,
    };

    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => ({ success: true, data: mockJobResponse }),
    } as Response);

    const result = await enqueueMobileOptimization({
      cvDocumentId: 'cv-doc-1',
      jobDescriptionId: 'job-desc-1',
      idempotencyKey: 'idem-123',
    });

    expect(result.jobId).toBe('job-123');
    expect(result.status).toBe('QUEUED');
    expect(global.fetch).toHaveBeenCalledWith(
      'http://localhost:8080/api/v1/jobs/cv-optimization',
      expect.objectContaining({
        method: 'POST',
        headers: expect.objectContaining({
          'Content-Type': 'application/json',
          'X-Correlation-ID': expect.stringMatching(/^mobile-\d+/),
        }),
      })
    );
  });

  it('pollMobileJobStatus queries GET /api/v1/jobs/{jobId}', async () => {
    const mockStatusResponse = {
      jobId: 'job-123',
      status: 'COMPLETED',
      resultJson: '{"atsScore":95}',
      errorMessage: null,
    };

    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => ({ success: true, data: mockStatusResponse }),
    } as Response);

    const result = await pollMobileJobStatus('job-123');
    expect(result.status).toBe('COMPLETED');
    expect(global.fetch).toHaveBeenCalledWith(
      'http://localhost:8080/api/v1/jobs/job-123',
      expect.objectContaining({
        headers: expect.objectContaining({
          'X-Correlation-ID': expect.stringMatching(/^mobile-\d+/),
        }),
      })
    );
  });
});
