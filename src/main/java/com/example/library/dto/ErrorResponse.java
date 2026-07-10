package com.example.library.dto;

import java.time.Instant;
import java.util.List;

/**
 * Structured error payload returned by {@code GlobalExceptionHandler} for every failed request.
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldError> fieldErrors
) {
    public record FieldError(String field, String message) {
    }
}
