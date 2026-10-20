package com.rysoo.api.dto;

import java.util.List;

public record ApiError(
    String code,
    String message,
    List<Record> details
) {
    public ApiError(String code, String message) {
        this(code, message, List.of());
    }
}
