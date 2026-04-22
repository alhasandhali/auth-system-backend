package com.backend.app.auth.authappbackend.dto;

import com.backend.app.auth.authappbackend.entity.Provider;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Data Transfer Object representing a User")
public class UserDto {
    @Schema(description = "Unique identifier of the user", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Full name of the user", example = "John Doe")
    private String name;

    @Schema(description = "Email address of the user", example = "john.doe@example.com")
    private String email;

    @Schema(description = "User password (only used for registration)", accessMode = Schema.AccessMode.WRITE_ONLY, example = "P@ssw0rd123")
    private String password;

    @Schema(description = "URL to user's profile image", example = "https://example.com/avatar.jpg")
    private String image;

    @Schema(description = "Whether the user account is enabled", example = "true")
    private boolean enabled = true;

    @Schema(description = "Timestamp when the user was created", example = "2024-01-01T00:00:00Z")
    private Instant created = Instant.now();

    @Schema(description = "Timestamp when the user was last updated", example = "2024-01-01T00:00:00Z")
    private Instant updated = Instant.now();

    @Schema(description = "Authentication provider used by the user", example = "LOCAL")
    private Provider provider = Provider.LOCAL;

    @Schema(description = "Roles assigned to the user")
    private Set<RoleDto> roles = new HashSet<>();
}
