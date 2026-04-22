package com.backend.app.auth.authappbackend.controller;

import com.backend.app.auth.authappbackend.dto.RoleDto;
import com.backend.app.auth.authappbackend.entity.Role;
import com.backend.app.auth.authappbackend.exception.ApiError;
import com.backend.app.auth.authappbackend.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/role")
@RequiredArgsConstructor
@Tag(name = "Role Management", description = "Endpoints for managing user roles (RBAC).")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {
    private final RoleService roleService;

    @Operation(
            summary = "Create a new role",
            description = "Creates a new role in the system. Restricted to ADMIN.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Role created",
                            content = @Content(schema = @Schema(implementation = RoleDto.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden - Admin only")
            }
    )
    @PostMapping
    public ResponseEntity<RoleDto> createRole(@RequestBody RoleDto roleDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roleService.createRole(roleDto));
    }

    @Operation(
            summary = "Get all roles",
            description = "Returns a list of all roles available in the system. Restricted to ADMIN.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Role.class))))
            }
    )
    @GetMapping
    public ResponseEntity<Iterable<Role>> getAllRole() {
        return ResponseEntity.status(HttpStatus.OK).body(roleService.getAllRoles());
    }

    @Operation(
            summary = "Get role by name",
            description = "Retrieves role details by its unique name.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success",
                            content = @Content(schema = @Schema(implementation = RoleDto.class))),
                    @ApiResponse(responseCode = "404", description = "Role not found",
                            content = @Content(schema = @Schema(implementation = ApiError.class)))
            }
    )
    @GetMapping("/roleName/{name}")
    public ResponseEntity<RoleDto> getRoleByName(
            @Parameter(description = "Name of the role", example = "ROLE_USER")
            @PathVariable String name) {
        return ResponseEntity.status(HttpStatus.OK).body(roleService.getRoleByName(name));
    }

    @Operation(
            summary = "Get role by ID",
            description = "Retrieves role details by its unique UUID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success",
                            content = @Content(schema = @Schema(implementation = RoleDto.class))),
                    @ApiResponse(responseCode = "404", description = "Role not found",
                            content = @Content(schema = @Schema(implementation = ApiError.class)))
            }
    )
    @GetMapping("/roleId/{id}")
    public ResponseEntity<RoleDto> getRoleById(
            @Parameter(description = "UUID of the role")
            @PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.OK).body(roleService.getRoleById(id));
    }

    @Operation(
            summary = "Delete role",
            description = "Deletes a role from the system by its ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Role deleted"),
                    @ApiResponse(responseCode = "404", description = "Role not found")
            }
    )
    @DeleteMapping("/{id}")
    public void deleteRole(@PathVariable UUID id) {
        roleService.deleteRole(id);
    }

    @Operation(
            summary = "Update role",
            description = "Updates an existing role's details.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Update successful",
                            content = @Content(schema = @Schema(implementation = RoleDto.class))),
                    @ApiResponse(responseCode = "404", description = "Role not found")
            }
    )
    @PatchMapping("/{id}")
    public ResponseEntity<RoleDto> updateRole(
            @RequestBody RoleDto roleDto,
            @Parameter(description = "UUID of the role to update")
            @PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.OK).body(roleService.updateRole(id, roleDto));
    }
}
