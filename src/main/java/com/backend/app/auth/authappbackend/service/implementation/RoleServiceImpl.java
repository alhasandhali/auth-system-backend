package com.backend.app.auth.authappbackend.service.implementation;

import com.backend.app.auth.authappbackend.dto.RoleDto;
import com.backend.app.auth.authappbackend.entity.Role;
import com.backend.app.auth.authappbackend.repository.RoleRepository;
import com.backend.app.auth.authappbackend.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;

    @Override
    public RoleDto createRole(RoleDto roleDto) {
        if (roleRepository.findByName(roleDto.getName()).isPresent()) {
            throw new RuntimeException("Role already exists");
        }
        Role role = modelMapper.map(roleDto, Role.class);
        roleRepository.save(role);
        return modelMapper.map(role, RoleDto.class);
    }

    @Override
    public RoleDto updateRole(UUID id, RoleDto roleDto) {

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        role.setName(roleDto.getName());

        Role saved = roleRepository.save(role);

        return modelMapper.map(saved, RoleDto.class);
    }

    @Override
    public void deleteRole(UUID id) {

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        roleRepository.delete(role);
    }

    @Override
    public RoleDto getRoleById(UUID id) {
        if (roleRepository.findById(id).isEmpty()) {
            throw new RuntimeException("Role not found!");
        }
        return modelMapper.map(roleRepository.findById(id).get(), RoleDto.class);
    }

    @Override
    public RoleDto getRoleByName(String name) {
        if (roleRepository.findByName(name).isEmpty()) {
            throw new RuntimeException("Role not found!");
        }
        return modelMapper.map(roleRepository.findByName(name).get(), RoleDto.class);
    }

    @Override
    public Iterable<Role> getAllRoles() {
        return roleRepository.findAll();
    }
}
