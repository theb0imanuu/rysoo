# Operations & Runbooks — Rysoo CV Optimization Platform

## 1. Health Checks & Observability

- **Liveness Probe**: `GET /actuator/health/liveness` — returns `{"status": "UP"}` if JVM and Web thread pool are responsive.
- **Readiness Probe**: `GET /actuator/health/readiness` — checks database connection pool and storage reachability.
- **Metrics**: Exposed at `/actuator/prometheus` for scraping CPU, memory, JVM heap, garbage collection, and custom meters:
  - `jobs.processed.count{type, status}`
  - `jobs.duration.seconds{type}`
  - `ai.requests.count{provider, model, status}`
  - `ai.cost.estimated{provider}`

---

## 2. Background Queue Operations & Crash Recovery

The PostgreSQL-backed queue (`processing_jobs`) guarantees durability without requiring external message brokers:
1. **Job Claiming**:
   ```sql
   UPDATE processing_jobs
   SET status = 'PROCESSING',
       locked_at = NOW(),
       locked_by = :workerId,
       updated_at = NOW()
   WHERE id = (
     SELECT id FROM processing_jobs
     WHERE status IN ('QUEUED', 'RETRYING')
       AND next_run_at <= NOW()
     ORDER BY priority DESC, created_at ASC
     FOR UPDATE SKIP LOCKED
     LIMIT 1
   )
   RETURNING *;
   ```
2. **Abandoned Job Recovery**:
   - A scheduled cleanup thread runs every 60 seconds.
   - Any job with `status = 'PROCESSING'` and `locked_at < NOW() - INTERVAL '10 MINUTES'` is considered abandoned due to worker crash or restart.
   - The reaper increments `attempt_count` and transitions the job to `RETRYING` (with exponential backoff) or `FAILED` (if `attempt_count >= max_retries`).

---

## 3. Incident Response Runbooks

### Runbook 1: High AI Failure Rate / Provider Outage
- **Symptoms**: Alert triggered for `ai.requests.count{status="error"} > 10%`.
- **Action**:
  1. Inspect worker logs for provider HTTP response codes (e.g., 429 Rate Limited, 503 Service Unavailable).
  2. Switch active provider via environment variable `AI_PRIMARY_PROVIDER=anthropic` (or `openai`/`gemini`).
  3. Deploy configuration without redeploying code.

### Runbook 2: Stuck Processing Jobs
- **Symptoms**: User reports CV parsing or optimization not completing.
- **Action**:
  1. Check `processing_jobs` status distribution:
     `SELECT status, count(*) FROM processing_jobs GROUP BY status;`
  2. If jobs are locked by an old worker, trigger recovery:
     `UPDATE processing_jobs SET status = 'RETRYING', locked_by = NULL WHERE status = 'PROCESSING' AND locked_at < NOW() - INTERVAL '10 minutes';`

### Runbook 3: Database Backup & Restore
- Automated daily backups are maintained in Supabase.
- Manual point-in-time snapshot before running major schema migrations:
  `supabase db dump -f backup_pre_release.sql`
