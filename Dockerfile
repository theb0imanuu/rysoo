# syntax=docker/dockerfile:1

################################################################################
# Stage 1: Build the Maven multi-module applications
################################################################################
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /build

# Copy root POM and module POMs first to cache dependency resolution
COPY pom.xml ./
COPY services/cv-domain/pom.xml services/cv-domain/
COPY services/cv-application/pom.xml services/cv-application/
COPY services/cv-infrastructure/pom.xml services/cv-infrastructure/
COPY services/cv-document-processing/pom.xml services/cv-document-processing/
COPY apps/api/pom.xml apps/api/
COPY apps/worker/pom.xml apps/worker/

# Pre-fetch Maven dependencies
RUN mvn -B dependency:go-offline -DexcludeGroupIds=com.rysoo || true

# Copy all source files
COPY services/ services/
COPY apps/ apps/

# Build and package fat JARs skipping tests in container build (tests are validated in CI)
RUN mvn -B clean package -DskipTests

################################################################################
# Stage 2: Unified Runtime Base
################################################################################
FROM eclipse-temurin:21-jre-jammy AS runtime

# Create non-root system group and user
RUN groupadd -g 10001 appgroup && \
    useradd -u 10001 -g appgroup -m -s /bin/sh appuser

WORKDIR /app

# Install curl for container healthcheck probes
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

# Copy packaged JARs for both API and Worker
COPY --from=builder --chown=appuser:appgroup /build/apps/api/target/api-1.0.0-SNAPSHOT.jar /app/api.jar
COPY --from=builder --chown=appuser:appgroup /build/apps/worker/target/worker-1.0.0-SNAPSHOT.jar /app/worker.jar

# Create reliable Linux entrypoint script (Unix LF line endings)
RUN printf '#!/bin/sh\nset -e\nif [ "$#" -gt 0 ]; then\n  exec "$@"\nelif [ "$SERVICE_TYPE" = "worker" ]; then\n  echo "Starting Rysoo Background Worker..."\n  exec java $JAVA_OPTS -jar /app/worker.jar\nelse\n  echo "Starting Rysoo REST API on port ${PORT:-8080}..."\n  exec java $JAVA_OPTS -jar /app/api.jar\nfi\n' > /app/entrypoint.sh && \
    chmod +x /app/entrypoint.sh && \
    chown appuser:appgroup /app/entrypoint.sh

USER appuser:appgroup

ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"
ENV SERVICE_TYPE="api"
ENV PORT="8080"

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:${PORT:-8080}/actuator/health || exit 1

ENTRYPOINT ["/app/entrypoint.sh"]
CMD []

################################################################################
# Stage 3: Specific target for API (backwards-compatible with CI workflow)
################################################################################
FROM runtime AS runtime-api
ENV SERVICE_TYPE="api"

################################################################################
# Stage 4: Specific target for Worker (backwards-compatible with CI workflow)
################################################################################
FROM runtime AS runtime-worker
ENV SERVICE_TYPE="worker"

################################################################################
# Stage 5: Final Default Target for Render builds (Defaults to API, respects $SERVICE_TYPE)
################################################################################
FROM runtime AS final
