package com.backend.app.auth.authappbackend.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DotenvConfig {
    static {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        set("DB_URL", dotenv);
        set("DB_USER", dotenv);
        set("DB_PASSWORD", dotenv);

        set("JWT_SECRET", dotenv);
        set("JWT_ISSUER", dotenv);
        set("JWT_ACCESS_TTL_SECONDS", dotenv);
        set("JWT_REFRESH_TTL_SECONDS", dotenv);
        set("JWT_REFRESH_COOKIE_NAME", dotenv);
        set("JWT_COOKIE_SECURE", dotenv);
        set("JWT_COOKIE_HTTP_ONLY", dotenv);
        set("JWT_COOKIE_SAME_SITE", dotenv);
        set("COOKIE_DOMAIN", dotenv);

        set("GOOGLE_CLIENT_ID", dotenv);
        set("GOOGLE_CLIENT_SECRET", dotenv);
        set("GITHUB_CLIENT_ID", dotenv);
        set("GITHUB_CLIENT_SECRET", dotenv);

        set("CORS_ALLOWED_ORIGINS", dotenv);
        set("FRONTEND_URL", dotenv);
        set("SPRING_PROFILES_ACTIVE", dotenv);
    }

    private static void set(String key, Dotenv dotenv) {
        String value = dotenv.get(key);

        if (value != null && !value.isBlank()) {
            System.setProperty(key, value);
        }
    }
}
