package com.backend.app.auth.authappbackend.exception;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public record ErrorResponse(
        String message,
        HttpStatus status,
        int statusCode,
        LocalDateTime localDateTime
) {
}
