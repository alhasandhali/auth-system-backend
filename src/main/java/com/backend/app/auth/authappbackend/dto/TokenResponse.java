package com.backend.app.auth.authappbackend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response object containing authentication tokens and user details")
public record TokenResponse(
        @Schema(description = "JWT Access Token", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,

        @Schema(description = "Refresh Token (can be used to get new access tokens)", example = "a7b8c9d0...")
        String refreshToken,

        @Schema(description = "Type of the token", example = "Bearer")
        String tokenType,

        @Schema(description = "Time in seconds until the access token expires", example = "3600")
        long expiresIn,

        @Schema(description = "Details of the authenticated user")
        UserDto user
) {

    public static TokenResponse of(String accessToken, String refreshToken, long expiresIn, UserDto user) {
        return new TokenResponse(accessToken, refreshToken, "Bearer", expiresIn, user);
    }
}
