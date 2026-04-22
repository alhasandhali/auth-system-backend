package com.backend.app.auth.authappbackend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Authentication Service API",
                description = "Comprehensive Authentication and Authorization Service. " +
                        "Provides JWT-based authentication, OAuth2 integration (Google/GitHub), " +
                        "Role-Based Access Control (RBAC) and Session Management via Refresh Tokens.",
                version = "1.0.0",
                contact = @Contact(
                        name = "Al Hasan Dhali",
                        url = "https://alhasandhali.vercel.app/",
                        email = "alhasandhali@gmail.com"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0"
                ),
                summary = "Identity and Access Management (IAM) API"
        ),
        servers = {
                @Server(description = "Local Development", url = "http://localhost:8080"),
                @Server(description = "Staging Environment", url = "https://staging-auth-api.example.com")
        },
        security = {
                @SecurityRequirement(name = "bearerAuth"),
                @SecurityRequirement(name = "refreshToken")
        }
)
@SecuritySchemes({
        @SecurityScheme(
                name = "bearerAuth",
                description = "JWT Access Token used for Authorization header",
                type = SecuritySchemeType.HTTP,
                scheme = "bearer",
                bearerFormat = "JWT",
                in = SecuritySchemeIn.HEADER
        ),
        @SecurityScheme(
                name = "refreshToken",
                description = "Refresh Token stored in Secure, HttpOnly cookie",
                type = SecuritySchemeType.APIKEY,
                in = SecuritySchemeIn.COOKIE
        )
})
public class ApiDocConfig {
}
