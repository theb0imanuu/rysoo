package com.rysoo.api.controller;

import com.rysoo.api.dto.ApiResponse;
import com.rysoo.api.filter.CorrelationIdFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health", description = "Liveness and Readiness Probes")
public class HealthController {

    @GetMapping
    @Operation(summary = "Check API health status")
    public ResponseEntity<ApiResponse<Map<String, String>>> checkHealth() {
        String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
        Map<String, String> healthData = Map.of(
                "status", "UP",
                "service", "rysoo-api",
                "version", "1.0.0"
        );
        return ResponseEntity.ok(ApiResponse.ok(healthData, correlationId));
    }
}
