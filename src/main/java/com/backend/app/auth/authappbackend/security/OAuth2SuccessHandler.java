package com.backend.app.auth.authappbackend.security;

import com.backend.app.auth.authappbackend.entity.Provider;
import com.backend.app.auth.authappbackend.entity.RefreshToken;
import com.backend.app.auth.authappbackend.entity.User;
import com.backend.app.auth.authappbackend.repository.RefreshTokenRepository;
import com.backend.app.auth.authappbackend.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CookieService cookieService;

    @org.springframework.beans.factory.annotation.Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Authentication authentication)
            throws IOException {

        if (!(authentication instanceof OAuth2AuthenticationToken token)) {
            throw new RuntimeException("Authentication is not OAuth2");
        }

        OAuth2User oAuth2User = token.getPrincipal();
        String registerId = token.getAuthorizedClientRegistrationId();

        User user;

        if ("google".equals(registerId)) {

            assert oAuth2User != null;
            String email = (String) oAuth2User.getAttributes().get("email");
            String name = (String) oAuth2User.getAttributes().get("name");
            String picture = (String) oAuth2User.getAttributes().get("picture");

            user = userRepository.findByEmail(email)
                    .orElseGet(() -> userRepository.save(
                            User.builder()
                                    .email(email)
                                    .name(name)
                                    .provider(Provider.GOOGLE)
                                    .image(picture)
                                    .enabled(true)
                                    .password("")
                                    .build()
                    ));
        }

        else if ("github".equals(registerId)) {

            assert oAuth2User != null;
            String email = (String) oAuth2User.getAttributes().get("email");
            String name = (String) oAuth2User.getAttributes().get("name");
            String picture = (String) oAuth2User.getAttributes().get("avatar_url");

            if (email == null || email.isBlank()) {
                email = oAuth2User.getAttributes().get("login") + "@github.local";
            }

            String finalEmail = email;
            user = userRepository.findByEmail(email)
                    .orElseGet(() -> userRepository.save(
                            User.builder()
                                    .email(finalEmail)
                                    .name(name)
                                    .provider(Provider.GITHUB)
                                    .image(picture)
                                    .enabled(true)
                                    .password("")
                                    .build()
                    ));
        }

        else {
            throw new RuntimeException("Unsupported provider");
        }

        if (!user.isEnabled()) {
            response.sendRedirect(frontendUrl + "/login");
            return;
        }

        // Create refresh token
        String jti = UUID.randomUUID().toString();

        RefreshToken refreshTokenOb = RefreshToken.builder()
                .jti(jti)
                .user(user)
                .revoked(false)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
                .build();

        refreshTokenRepository.save(refreshTokenOb);

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(user, jti);
        String refreshToken = jwtService.generateRefreshToken(user, jti);

        // Attach refresh token cookie
        cookieService.attachedRefreshCookie(
                response,
                refreshToken,
                (int) jwtService.getRefreshTtlSeconds()
        );

        response.sendRedirect(
                frontendUrl + "/oauth-success"
        );
    }
}