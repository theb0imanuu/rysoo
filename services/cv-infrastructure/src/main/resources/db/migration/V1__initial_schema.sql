-- Rysoo CV Optimization Platform - Initial Schema (V1)
-- Forward-only PostgreSQL migration with UUIDs and explicit ownership

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 1. Profiles (matches Supabase Auth user UUID)
CREATE TABLE IF NOT EXISTS profiles (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 2. CV Documents (Container owned by user)
CREATE TABLE IF NOT EXISTS cv_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    archived_at TIMESTAMPTZ NULL
);
CREATE INDEX IF NOT EXISTS idx_cv_documents_user_id ON cv_documents(user_id);

-- 3. CV Versions (Immutable versions)
CREATE TABLE IF NOT EXISTS cv_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cv_document_id UUID NOT NULL REFERENCES cv_documents(id) ON DELETE CASCADE,
    version_number INT NOT NULL,
    version_type VARCHAR(32) NOT NULL,
    storage_path VARCHAR(512),
    file_name VARCHAR(255),
    file_size_bytes BIGINT,
    mime_type VARCHAR(128),
    content_hash_sha256 VARCHAR(64) NOT NULL,
    raw_text TEXT,
    normalized_markdown TEXT NOT NULL,
    structured_ast_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_cv_version UNIQUE (cv_document_id, version_number)
);
CREATE INDEX IF NOT EXISTS idx_cv_versions_doc_ver ON cv_versions(cv_document_id, version_number);

-- 4. Job Descriptions
CREATE TABLE IF NOT EXISTS job_descriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    target_role VARCHAR(255) NOT NULL,
    target_company VARCHAR(255),
    raw_text TEXT NOT NULL,
    parsed_requirements_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_job_descriptions_user_id ON job_descriptions(user_id);

-- 5. Optimization Projects
CREATE TABLE IF NOT EXISTS optimization_projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    cv_document_id UUID NOT NULL REFERENCES cv_documents(id) ON DELETE CASCADE,
    job_description_id UUID NOT NULL REFERENCES job_descriptions(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_optimization_projects_user_id ON optimization_projects(user_id);

-- 6. Optimization Runs (Tracks attempts, models, tokens, cost)
CREATE TABLE IF NOT EXISTS optimization_runs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES optimization_projects(id) ON DELETE CASCADE,
    source_cv_version_id UUID NOT NULL REFERENCES cv_versions(id) ON DELETE RESTRICT,
    job_description_id UUID NOT NULL REFERENCES job_descriptions(id) ON DELETE RESTRICT,
    ai_provider VARCHAR(64) NOT NULL,
    ai_model_identifier VARCHAR(128) NOT NULL,
    prompt_version VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    attempt_count INT NOT NULL DEFAULT 0,
    token_usage_input INT NOT NULL DEFAULT 0,
    token_usage_output INT NOT NULL DEFAULT 0,
    estimated_cost_usd NUMERIC(10, 6) NOT NULL DEFAULT 0.000000,
    error_code VARCHAR(64),
    sanitized_error_message TEXT,
    started_at TIMESTAMPTZ NULL,
    completed_at TIMESTAMPTZ NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_optimization_runs_project_id ON optimization_runs(project_id);
CREATE INDEX IF NOT EXISTS idx_optimization_runs_status ON optimization_runs(status);

-- 7. Optimization Results
CREATE TABLE IF NOT EXISTS optimization_results (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    optimization_run_id UUID NOT NULL UNIQUE REFERENCES optimization_runs(id) ON DELETE CASCADE,
    tailored_markdown TEXT NOT NULL,
    evidence_mapping_json JSONB NOT NULL,
    match_score_percentage INT NOT NULL,
    match_explanation_markdown TEXT NOT NULL,
    factual_verification_status VARCHAR(32) NOT NULL,
    verification_report_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    diff_summary_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 8. Export Files
CREATE TABLE IF NOT EXISTS export_files (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    optimization_result_id UUID NOT NULL REFERENCES optimization_results(id) ON DELETE CASCADE,
    format VARCHAR(16) NOT NULL,
    storage_path VARCHAR(512) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    mime_type VARCHAR(128) NOT NULL,
    download_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_export_files_user_id ON export_files(user_id);

-- 9. Usage Ledger
CREATE TABLE IF NOT EXISTS usage_ledger (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    operation_type VARCHAR(64) NOT NULL,
    reference_id UUID NULL,
    units_consumed INT NOT NULL DEFAULT 1,
    estimated_cost_usd NUMERIC(10, 6) NOT NULL DEFAULT 0.000000,
    metadata_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_usage_ledger_user_id ON usage_ledger(user_id, created_at);

-- 10. Processing Jobs (Durable Queue)
CREATE TABLE IF NOT EXISTS processing_jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_type VARCHAR(64) NOT NULL,
    user_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    payload_json JSONB NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'QUEUED',
    priority INT NOT NULL DEFAULT 0,
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    attempt_count INT NOT NULL DEFAULT 0,
    max_retries INT NOT NULL DEFAULT 3,
    next_run_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    locked_at TIMESTAMPTZ NULL,
    locked_by VARCHAR(128) NULL,
    result_json JSONB NULL,
    error_message TEXT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_jobs_queue_poll ON processing_jobs(status, next_run_at, priority DESC);
