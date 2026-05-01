package com.john.ledger.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins:https://myledger.techseek.in}")
    private String allowedOrigins;

    @Value("${app.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS,PATCH}")
    private String allowedMethods;

    @Value("${app.cors.allowed-headers:*}")
    private String allowedHeaders;

    @Value("${app.cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Value("${app.cors.max-age:3600}")
    private long maxAge;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        // ===============================
        // Allowed Origins
        // ===============================
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        for (String origin : origins) {
            config.addAllowedOrigin(origin);
        }

        // Local testing optional
        config.addAllowedOrigin("http://localhost:4200");
        config.addAllowedOrigin("http://localhost:3000");

        // ===============================
        // Allowed Methods
        // ===============================
        List<String> methods = Arrays.stream(allowedMethods.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        for (String method : methods) {
            config.addAllowedMethod(method);
        }

        // ===============================
        // Allowed Headers
        // ===============================
        if ("*".equals(allowedHeaders.trim())) {
            config.addAllowedHeader("*");
        } else {
            List<String> headers = Arrays.stream(allowedHeaders.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();

            for (String header : headers) {
                config.addAllowedHeader(header);
            }
        }

        // Must allow JWT / JSON headers
        config.addAllowedHeader("Authorization");
        config.addAllowedHeader("Content-Type");
        config.addAllowedHeader("Accept");
        config.addAllowedHeader("Origin");
        config.addAllowedHeader("X-Requested-With");

        // ===============================
        // Exposed Headers
        // ===============================
        config.addExposedHeader("Authorization");
        config.addExposedHeader("Content-Disposition");

        // ===============================
        // Credentials
        // ===============================
        config.setAllowCredentials(allowCredentials);

        // ===============================
        // Preflight Cache
        // ===============================
        config.setMaxAge(maxAge);

        // ===============================
        // Apply All URLs
        // ===============================
        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }
}