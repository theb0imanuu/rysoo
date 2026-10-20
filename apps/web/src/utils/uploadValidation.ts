export interface ValidationResult {
  valid: boolean;
  error?: string;
}

export const MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10MB

export const ALLOWED_EXTENSIONS = ['.pdf', '.docx', '.md'];

export const ALLOWED_MIME_TYPES = [
  'application/pdf',
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
  'text/markdown',
  'text/plain',
];

export function validateCvFile(fileName: string, sizeBytes: number, mimeType?: string): ValidationResult {
  if (!fileName || fileName.trim() === '') {
    return { valid: false, error: 'File name cannot be empty' };
  }

  const lowerName = fileName.toLowerCase();
  const hasValidExt = ALLOWED_EXTENSIONS.some(ext => lowerName.endsWith(ext));
  if (!hasValidExt) {
    return {
      valid: false,
      error: `Unsupported file extension. Only ${ALLOWED_EXTENSIONS.join(' and ')} files are allowed.`,
    };
  }

  if (sizeBytes <= 0) {
    return { valid: false, error: 'File is empty' };
  }

  if (sizeBytes > MAX_FILE_SIZE_BYTES) {
    return {
      valid: false,
      error: `File size exceeds the 10MB maximum limit (${(sizeBytes / (1024 * 1024)).toFixed(1)}MB).`,
    };
  }

  if (mimeType && !ALLOWED_MIME_TYPES.includes(mimeType)) {
    return {
      valid: false,
      error: 'Invalid file MIME type. Only PDF and DOCX files are supported.',
    };
  }

  return { valid: true };
}
