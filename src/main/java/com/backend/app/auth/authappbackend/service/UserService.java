package com.backend.app.auth.authappbackend.service;

import com.backend.app.auth.authappbackend.dto.UserDto;

import java.util.UUID;

public interface UserService {

//    Create User
    UserDto createUser(UserDto userDto);

//    Update User
    UserDto updateUser(UserDto userDto, String userId);

//    Delete User
    void deleteUser(String userId);

//    Get User
    UserDto getUserById(String userId);
    UserDto getUserByEmail(String email);
    Iterable<UserDto> getAllUsers();

//    Assign Role
    UserDto assignRoleToUserById(UUID userId, String roleName);
    UserDto assignRoleToUserByEmail(String email, String roleName);
}
