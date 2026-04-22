package com.backend.app.auth.authappbackend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request object to obtain a new access token using a refresh token")
public record RefreshRequest(
        @Schema(description = "The refresh token string", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String refreshToken
) {
}
