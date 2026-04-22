package com.backend.app.auth.authappbackend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Data Transfer Object for User Roles")
public class RoleDto {
    @Schema(description = "Unique identifier of the role", example = "a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d")
    private UUID id;

    @Schema(description = "Name of the role", example = "ROLE_USER")
    private String name;
}
