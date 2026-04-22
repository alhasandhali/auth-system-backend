package com.backend.app.auth.authappbackend.security;

import com.backend.app.auth.authappbackend.repository.UserRepository;
import com.backend.app.auth.authappbackend.util.UserIdParse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    private final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.equals("/api/v1/auth/login") || 
            path.equals("/api/v1/auth/register") || 
            path.equals("/api/v1/auth/refresh")) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {

            // 1. Validate token
            if (!jwtService.isAccessTokenValid(token)) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            // 2. Parse claims
            Jws<Claims> parsed = jwtService.parseToken(token);
            Claims claims = parsed.getPayload();

            UUID userId = UserIdParse.parseUUID(claims.getSubject());

            // 3. Load user
            var userOpt = userRepository.findById(userId);

            if (userOpt.isEmpty()) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            var user = userOpt.get();

            // 4. Check user status
            if (!user.isEnabled()) {
                logger.warn("Disabled user tried access: {}", userId);
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            // 5. Build authorities
            List<GrantedAuthority> authorities =
                    user.getRoles() == null
                            ? List.of()
                            : user.getRoles().stream()
                              .map(role -> new SimpleGrantedAuthority(role.getName()))
                              .collect(Collectors.toList());

            // 6. Create authentication
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            authorities
                    );

            auth.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (ExpiredJwtException ex) {
            logger.warn("Token expired");
            SecurityContextHolder.clearContext();

        } catch (Exception ex) {
            logger.error("JWT error", ex);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/api/v1/auth/login") || 
               path.equals("/api/v1/auth/register") || 
               path.equals("/api/v1/auth/refresh");
    }
}