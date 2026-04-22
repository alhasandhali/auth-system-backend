package com.backend.app.auth.authappbackend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request object for user authentication")
public record LoginRequest(
        @Schema(description = "User's email address", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        String email,
        
        @Schema(description = "User's password", example = "Password123!", requiredMode = Schema.RequiredMode.REQUIRED)
        String password
) {
}
