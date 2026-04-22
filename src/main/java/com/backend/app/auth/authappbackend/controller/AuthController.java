package com.backend.app.auth.authappbackend.controller;

import com.backend.app.auth.authappbackend.dto.LoginRequest;
import com.backend.app.auth.authappbackend.dto.RefreshRequest;
import com.backend.app.auth.authappbackend.dto.TokenResponse;
import com.backend.app.auth.authappbackend.dto.UserDto;
import com.backend.app.auth.authappbackend.entity.RefreshToken;
import com.backend.app.auth.authappbackend.entity.User;
import com.backend.app.auth.authappbackend.exception.ApiError;
import com.backend.app.auth.authappbackend.exception.InvalidTokenException;
import com.backend.app.auth.authappbackend.repository.RefreshTokenRepository;
import com.backend.app.auth.authappbackend.repository.UserRepository;
import com.backend.app.auth.authappbackend.security.CookieService;
import com.backend.app.auth.authappbackend.security.JwtService;
import com.backend.app.auth.authappbackend.service.AuthService;
import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user login, registration, token refresh, and logout.")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final ModelMapper modelMapper;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CookieService cookieService;
    private final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private Authentication authenticate(LoginRequest loginResponse) {
        return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginResponse.email(), loginResponse.password()));
    }

    private Optional<String> readRefreshTokenFromRequest(
            RefreshRequest body,
            HttpServletRequest request) {

        // 1. BODY (highest priority if provided)
        if (body != null && body.refreshToken() != null && !body.refreshToken().isBlank()) {
            return Optional.of(body.refreshToken().trim());
        }

        // 2. HEADER
        String headerToken = request.getHeader("X-Refresh-Token");
        if (headerToken != null && !headerToken.isBlank()) {
            return Optional.of(headerToken.trim());
        }

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            String token = authHeader.substring(7).trim();
            if (!token.isEmpty()) {
                return Optional.of(token);
            }
        }

        // 3. COOKIE (last fallback)
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(c -> cookieService.getRefreshTokenCookieName().equals(c.getName()))
                    .map(Cookie::getValue)
                    .filter(v -> v != null && !v.isBlank())
                    .findFirst();
        }

        return Optional.empty();
    }

    @Operation(
            summary = "Authenticate user",
            description = "Logs in a user with email and password, returning access and refresh tokens.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully authenticated",
                            content = @Content(schema = @Schema(implementation = TokenResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials",
                            content = @Content(schema = @Schema(implementation = ApiError.class))),
                    @ApiResponse(responseCode = "403", description = "User account disabled",
                            content = @Content(schema = @Schema(implementation = ApiError.class)))
            }
    )
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @RequestBody LoginRequest loginRequest,
            HttpServletResponse response) {
        Authentication authentication = authenticate(loginRequest);
        User u = (User) authentication.getPrincipal();

        User user = userRepository.findByEmail(loginRequest.email()).orElseThrow(() ->
                new BadCredentialsException("Invalid email or password"));

        if (!user.isEnabled()) {
            throw new DisabledException("User is disabled");
        }

        String jti = UUID.randomUUID().toString();

        var refreshTokenEntity = RefreshToken.builder()
                .jti(jti)
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshTokenEntity);

        String accessToken = jwtService.generateAccessToken(user, jti);
        String refreshToken = jwtService.generateRefreshToken(user, refreshTokenEntity.getJti());

//        Add Refresh token in cookie
        cookieService.attachedRefreshCookie(response, refreshToken, (int) jwtService.getRefreshTtlSeconds());
        cookieService.addNoStoreHeaders(response);

        TokenResponse tokenResponse = TokenResponse.of(accessToken, refreshToken, jwtService.getAccessTtlSeconds(), modelMapper.map(user, UserDto.class));

        return ResponseEntity.ok(tokenResponse);
    }

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account with the provided details.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User successfully registered",
                            content = @Content(schema = @Schema(implementation = UserDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data",
                            content = @Content(schema = @Schema(implementation = ApiError.class)))
            }
    )
    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@RequestBody UserDto userDto) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.registerUser(userDto));
    }

    @Operation(
            summary = "Refresh access token",
            description = "Obtains a new access token using a valid refresh token from body, header, or cookie.",
            security = @SecurityRequirement(name = "refreshToken"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Token successfully refreshed",
                            content = @Content(schema = @Schema(implementation = TokenResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token",
                            content = @Content(schema = @Schema(implementation = ApiError.class)))
            }
    )
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @RequestBody(required = false) RefreshRequest body,
            HttpServletRequest request,
            HttpServletResponse response) {

        String refreshToken = readRefreshTokenFromRequest(body, request)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (!jwtService.isRefreshTokenValid(refreshToken)) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        UUID userId = jwtService.getUserId(refreshToken);
        String jti = jwtService.getJtl(refreshToken);

        RefreshToken storedToken = refreshTokenRepository.findByJti(jti)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (storedToken.isRevoked()) {
            throw new InvalidTokenException("Refresh token revoked");
        }

        if (storedToken.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidTokenException("Refresh token expired");
        }

        if (!storedToken.getUser().getId().equals(userId)) {
            throw new InvalidTokenException("Token does not belong to user");
        }

        User user = storedToken.getUser();

//         NEW TOKEN ID
        String newJti = UUID.randomUUID().toString();

//        ONLY 1 ACTIVE SESSION PER USER
        refreshTokenRepository.revokeAllByUserId(user.getId());

//        ROTATE OLD TOKEN
        storedToken.setRevoked(true);
        storedToken.setReplaceByToken(newJti);
        refreshTokenRepository.save(storedToken);

//       CREATE NEW TOKEN ROW
        RefreshToken newToken = RefreshToken.builder()
                .jti(newJti)
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
                .revoked(false)
                .build();

        refreshTokenRepository.save(newToken);

//        JWT TOKENS
        String newAccessToken = jwtService.generateAccessToken(user, newJti);
        String newRefreshToken = jwtService.generateRefreshToken(user, newJti);

        cookieService.attachedRefreshCookie(
                response,
                newRefreshToken,
                (int) jwtService.getRefreshTtlSeconds()
        );

        cookieService.addNoStoreHeaders(response);

        return ResponseEntity.ok(
                TokenResponse.of(
                        newAccessToken,
                        newRefreshToken,
                        jwtService.getAccessTtlSeconds(),
                        modelMapper.map(user, UserDto.class)
                )
        );
    }

    @Operation(
            summary = "Logout user",
            description = "Revokes the current refresh token and clears authentication cookies.",
            security = @SecurityRequirement(name = "refreshToken"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "Successfully logged out"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized",
                            content = @Content(schema = @Schema(implementation = ApiError.class)))
            }
    )
    @Transactional
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        readRefreshTokenFromRequest(null, request)
                .ifPresent(token -> {
                    try {
                        if (jwtService.isRefreshTokenValid(token)) {
                            String jti = jwtService.getJtl(token);
                            refreshTokenRepository.findByJti(jti).ifPresent(refreshToken -> {
                                refreshTokenRepository.revokeByJti(jti);
                            });
                        }
                    } catch (JwtException e) {
                        logger.warn("Invalid refresh token", e);
                    }
                });

//        Clear Cookie
        cookieService.clearRefreshCookie(response);
        cookieService.addNoStoreHeaders(response);
        SecurityContextHolder.clearContext();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(
            summary = "Get current user profile",
            description = "Returns details of the currently authenticated user based on the access token.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success",
                            content = @Content(schema = @Schema(implementation = UserDto.class))),
                    @ApiResponse(responseCode = "401", description = "Invalid or missing access token",
                            content = @Content(schema = @Schema(implementation = ApiError.class)))
            }
    )
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {

        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new InvalidTokenException("Invalid access token");
        }

        return ResponseEntity.ok(Map.of(
                "user", modelMapper.map(user, UserDto.class)
        ));
    }
}


