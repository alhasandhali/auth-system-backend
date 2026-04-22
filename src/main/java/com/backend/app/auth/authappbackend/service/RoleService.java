package com.backend.app.auth.authappbackend.service;

import com.backend.app.auth.authappbackend.dto.RoleDto;
import com.backend.app.auth.authappbackend.entity.Role;

import java.util.UUID;

public interface RoleService {
    RoleDto createRole(RoleDto roleDto);
    RoleDto updateRole(UUID id, RoleDto roleDto);
    void deleteRole(UUID id);
    RoleDto getRoleById(UUID id);
    RoleDto getRoleByName(String name);
    Iterable<Role> getAllRoles();
}
