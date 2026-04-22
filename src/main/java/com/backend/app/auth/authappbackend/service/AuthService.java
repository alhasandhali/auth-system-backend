package com.backend.app.auth.authappbackend.service;

import com.backend.app.auth.authappbackend.dto.UserDto;

public interface AuthService {
    UserDto registerUser(UserDto userDto);

//    UserDto loginUser(UserDto userDto);
}
