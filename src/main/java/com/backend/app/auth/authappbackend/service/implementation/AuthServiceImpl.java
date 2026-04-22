package com.backend.app.auth.authappbackend.service.implementation;

import com.backend.app.auth.authappbackend.dto.UserDto;
import com.backend.app.auth.authappbackend.service.AuthService;
import com.backend.app.auth.authappbackend.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDto registerUser(UserDto userDto) {
        userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
        return userService.createUser(userDto);
    }
}
