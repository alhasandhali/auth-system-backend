package com.backend.app.auth.authappbackend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request object for assigning a role to a user")
public record RoleRequest(
        @Schema(description = "Name of the role to assign", example = "ROLE_ADMIN", requiredMode = Schema.RequiredMode.REQUIRED)
        String roleName
) {
}
