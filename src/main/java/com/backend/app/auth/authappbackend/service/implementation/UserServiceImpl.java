package com.backend.app.auth.authappbackend.service.implementation;

import com.backend.app.auth.authappbackend.dto.UserDto;
import com.backend.app.auth.authappbackend.entity.Provider;
import com.backend.app.auth.authappbackend.entity.Role;
import com.backend.app.auth.authappbackend.entity.User;
import com.backend.app.auth.authappbackend.exception.UserNotFoundException;
import com.backend.app.auth.authappbackend.repository.RoleRepository;
import com.backend.app.auth.authappbackend.repository.UserRepository;
import com.backend.app.auth.authappbackend.service.UserService;
import com.backend.app.auth.authappbackend.util.UserIdParse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {

//        Check the user already have an account or not
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User with this email already exists!");
        }

//        Check the user give email or not
        if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required!");
        }
//      TODO: Password will be Encrypted and Set the role
//        Check user give password or not
        if (userDto.getPassword() == null || userDto.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required!");
        }

//        Convert user DTO to user entity
        User user = modelMapper.map(userDto, User.class);

//        Set user provider
        user.setProvider(userDto.getProvider() != null ? userDto.getProvider() : Provider.LOCAL);

//        Save user on Database
        User savedUser = userRepository.save(user);

//        Convert and return the user
        return modelMapper.map(savedUser, UserDto.class);
    }

    @Override
    @Transactional
    public UserDto updateUser(UserDto userDto, String userId) {
        UUID uId = UserIdParse.parseUUID(userId);
        User existingUser = userRepository.findById(uId).orElseThrow(() -> new UserNotFoundException("User not found by the given id!"));

        if (userDto.getName() != null) existingUser.setName(userDto.getName());
        if (userDto.getImage() != null) existingUser.setImage(userDto.getImage());
        if (userDto.getProvider() != null) existingUser.setProvider(userDto.getProvider());
        existingUser.setEnabled(userDto.isEnabled());
//        TODO: Password will encrypted and set role
        if (userDto.getPassword() != null) existingUser.setPassword(userDto.getPassword());
//        if (userDto.getRoles() != null) existingUser.setRoles(userDto.getRoles())
        existingUser.setUpdated(Instant.now());

        User updatedUser = userRepository.save(existingUser);

        return modelMapper.map(updatedUser, UserDto.class);
    }

    @Override
    @Transactional
    public void deleteUser(String userId) {
        UUID uId = UserIdParse.parseUUID(userId);
        User user = userRepository.findById(uId).orElseThrow(() -> new UserNotFoundException("User not found by the given id!"));

        userRepository.delete(user);
    }

    @Override
    @Transactional
    public UserDto getUserById(String userId) {
        UUID uId = UserIdParse.parseUUID(userId);
        User user = userRepository.findById(uId).orElseThrow(() -> new UserNotFoundException("User not found by the given id!"));

        return modelMapper.map(user, UserDto.class);
    }

    @Override
    @Transactional
    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found by this email."));

        return modelMapper.map(user, UserDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<UserDto> getAllUsers() {
        return userRepository
                .findAll()
                .stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .toList();
    }

    @Override
    @Transactional
    public UserDto assignRoleToUserById(UUID userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.getRoles().add(role);

        User saved = userRepository.save(user);

        return modelMapper.map(saved, UserDto.class);
    }
    @Override
    @Transactional
    public UserDto assignRoleToUserByEmail(String email, String roleName) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.getRoles().add(role);

        User saved = userRepository.save(user);

        return modelMapper.map(saved, UserDto.class);
    }

}
