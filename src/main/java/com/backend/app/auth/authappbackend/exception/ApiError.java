package com.backend.app.auth.authappbackend.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Schema(description = "Standard error response structure")
public record ApiError(
        @Schema(description = "Detailed error message", example = "Invalid access token")
        String message,

        @Schema(description = "HTTP status name", example = "UNAUTHORIZED")
        HttpStatus status,

        @Schema(description = "HTTP status code", example = "401")
        int statusCode,

        @Schema(description = "Timestamp when the error occurred", example = "2024-01-01T12:00:00")
        LocalDateTime localDateTime,

        @Schema(description = "API path where the error occurred", example = "/api/v1/auth/me")
        String path
) {
    public static ApiError of(
            String message,
            HttpStatus status,
            int statusCode,
            String path) {
        return new ApiError(message, status, statusCode, LocalDateTime.now(), path);
    }
}
