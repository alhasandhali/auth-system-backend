package com.backend.app.auth.authappbackend.controller;

import com.backend.app.auth.authappbackend.dto.RoleRequest;
import com.backend.app.auth.authappbackend.dto.UserDto;
import com.backend.app.auth.authappbackend.exception.ApiError;
import com.backend.app.auth.authappbackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user")
@AllArgsConstructor
@Tag(name = "User Management", description = "Endpoints for managing users and their roles.")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Create a user",
            description = "Creates a new user record. Usually used by admins.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "User created",
                            content = @Content(schema = @Schema(implementation = UserDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid data",
                            content = @Content(schema = @Schema(implementation = ApiError.class)))
            }
    )
    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.createUser(userDto));
    }

    @Operation(
            summary = "Get all users",
            description = "Returns a list of all registered users. Restricted to ADMIN role.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserDto.class)))),
                    @ApiResponse(responseCode = "403", description = "Forbidden - Admin only",
                            content = @Content(schema = @Schema(implementation = ApiError.class)))
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Iterable<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Operation(
            summary = "Get user by email",
            description = "Retrieves a user's details by their email address. Accessible by ADMIN or the user themselves.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success",
                            content = @Content(schema = @Schema(implementation = UserDto.class))),
                    @ApiResponse(responseCode = "404", description = "User not found",
                            content = @Content(schema = @Schema(implementation = ApiError.class)))
            }
    )
    @PreAuthorize("hasRole('ADMIN') or #email == authentication.name")
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto> getUserByEmail(
            @Parameter(description = "Email of the user to retrieve", example = "user@example.com")
            @PathVariable String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @Operation(
            summary = "Get user by ID",
            description = "Retrieves a user's details by their unique ID. Accessible by ADMIN or the owner.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success",
                            content = @Content(schema = @Schema(implementation = UserDto.class))),
                    @ApiResponse(responseCode = "404", description = "User not found",
                            content = @Content(schema = @Schema(implementation = ApiError.class)))
            }
    )
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isOwner(#id, authentication)")
    @GetMapping("/id/{id}")
    public ResponseEntity<UserDto> getUserById(
            @Parameter(description = "UUID of the user", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Operation(
            summary = "Update user",
            description = "Updates user details. Only certain fields can be updated. Accessible by ADMIN or the owner.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Update successful",
                            content = @Content(schema = @Schema(implementation = UserDto.class))),
                    @ApiResponse(responseCode = "404", description = "User not found",
                            content = @Content(schema = @Schema(implementation = ApiError.class)))
            }
    )
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isOwner(#id, authentication)")
    @PatchMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(
            @Parameter(description = "UUID of the user to update")
            @PathVariable String id,
            @RequestBody UserDto userDto
    ) {
        return ResponseEntity.ok(userService.updateUser(userDto, id));
    }

    @Operation(
            summary = "Delete user",
            description = "Deletes a user account. Restricted to ADMIN role.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "User deleted"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - Admin only")
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/id/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Assign role by ID",
            description = "Assigns a specific role to a user using their ID. Restricted to ADMIN role.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Role assigned successfully",
                            content = @Content(schema = @Schema(implementation = UserDto.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden - Admin only")
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/id/{userId}/roles")
    public ResponseEntity<UserDto> assignRoleById(
            @PathVariable UUID userId,
            @RequestBody RoleRequest request
    ) {
        return ResponseEntity.ok(
                userService.assignRoleToUserById(userId, request.roleName())
        );
    }

    @Operation(
            summary = "Assign role by email",
            description = "Assigns a specific role to a user using their email. Restricted to ADMIN role.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Role assigned successfully",
                            content = @Content(schema = @Schema(implementation = UserDto.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden - Admin only")
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/email/{userMail}/roles")
    public ResponseEntity<UserDto> assignRoleByEmail(
            @PathVariable String userMail,
            @RequestBody RoleRequest request
    ) {
        return ResponseEntity.ok(
                userService.assignRoleToUserByEmail(userMail, request.roleName())
        );
    }

    @Operation(
            summary = "Get my profile",
            description = "Retrieves the profile of the currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success",
                            content = @Content(schema = @Schema(implementation = UserDto.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    @GetMapping("/me")
    public ResponseEntity<UserDto> getMyProfile(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }
}