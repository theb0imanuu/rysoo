package com.rysoo.api.dto;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

public record ApiResponse<T>(
    boolean success,
    T data,
    ApiError error,
    Meta meta
) {
    public record Meta(
        String correlationId,
        Instant timestamp
    ) {}

    public static <T> ApiResponse<T> ok(T data, String correlationId) {
        return new ApiResponse<>(true, data, null, new Meta(correlationId, Instant.now()));
    }

    public static <T> ApiResponse<T> ok(T data) {
        String correlationId = org.slf4j.MDC.get(com.rysoo.api.filter.CorrelationIdFilter.MDC_KEY);
        return ok(data, correlationId != null ? correlationId : java.util.UUID.randomUUID().toString());
    }

    public static <T> ApiResponse<T> error(String code, String message, List<Record> details, String correlationId) {
        return new ApiResponse<>(false, null, new ApiError(code, message, details), new Meta(correlationId, Instant.now()));
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        String correlationId = org.slf4j.MDC.get(com.rysoo.api.filter.CorrelationIdFilter.MDC_KEY);
        return error(code, message, Collections.emptyList(), correlationId != null ? correlationId : java.util.UUID.randomUUID().toString());
    }
}
