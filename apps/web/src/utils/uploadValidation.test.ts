import { describe, it, expect } from 'vitest';
import { validateCvFile, MAX_FILE_SIZE_BYTES } from './uploadValidation';

describe('validateCvFile', () => {
  it('accepts valid PDF within size limit', () => {
    const result = validateCvFile('resume.pdf', 1024 * 500, 'application/pdf');
    expect(result.valid).toBe(true);
    expect(result.error).toBeUndefined();
  });

  it('accepts valid DOCX within size limit', () => {
    const result = validateCvFile(
      'resume.docx',
      1024 * 800,
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
    );
    expect(result.valid).toBe(true);
    expect(result.error).toBeUndefined();
  });

  it('rejects unsupported file extensions', () => {
    const result = validateCvFile('malicious.exe', 1024, 'application/x-msdownload');
    expect(result.valid).toBe(false);
    expect(result.error).toContain('Unsupported file extension');
  });

  it('rejects file larger than 10MB', () => {
    const result = validateCvFile('giant-resume.pdf', MAX_FILE_SIZE_BYTES + 1, 'application/pdf');
    expect(result.valid).toBe(false);
    expect(result.error).toContain('exceeds the 10MB maximum limit');
  });

  it('rejects zero byte file', () => {
    const result = validateCvFile('empty.pdf', 0, 'application/pdf');
    expect(result.valid).toBe(false);
    expect(result.error).toBe('File is empty');
  });
});
