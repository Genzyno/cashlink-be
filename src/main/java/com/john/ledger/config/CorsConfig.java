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

    @Value("${app.cors.allowed-origins:*}")
    private String allowedOrigins;

    @Value("${app.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS,PATCH}")
    private String allowedMethods;

    @Value("${app.cors.allowed-headers:*}")
    private String allowedHeaders;

    @Value("${app.cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Value("${app.cors.max-age:3600}")
    private long maxAge;

    /**
     * CORS ConfigurationSource Bean - Returns the CORS configuration for all requests.
     * Used by Spring Security's SecurityFilterChain to properly handle CORS.
     * Allows specified origins and includes Authorization header in preflight responses.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Configure allowed origins
        String originsTrimmed = allowedOrigins != null ? allowedOrigins.trim() : "";
        if (originsTrimmed.isEmpty() || "*".equals(originsTrimmed)) {
            // Only use wildcard if credentials are NOT allowed (invalid combination otherwise)
            if (!allowCredentials) {
                config.addAllowedOriginPattern("*");
            } else {
                // With credentials=true, we must specify exact origins
                config.addAllowedOrigin("https://myledger.techseek.in");
                config.addAllowedOrigin("http://localhost:4200");
                config.addAllowedOrigin("http://localhost:3000");
                config.addAllowedOrigin("http://127.0.0.1:4200");
            }
        } else {
            List<String> origins = Arrays.asList(allowedOrigins.split(","));
            for (String origin : origins) {
                String trimmedOrigin = origin.trim();
                if (!trimmedOrigin.isEmpty()) {
                    if ("*".equals(trimmedOrigin)) {
                        if (!allowCredentials) {
                            config.addAllowedOriginPattern("*");
                        }
                    } else {
                        config.addAllowedOrigin(trimmedOrigin);
                    }
                }
            }
        }

        // Parse allowed methods
        List<String> methods = Arrays.asList(allowedMethods.split(","));
        for (String method : methods) {
            String trimmedMethod = method.trim();
            if (!trimmedMethod.isEmpty()) {
                config.addAllowedMethod(trimmedMethod);
            }
        }

        // Parse allowed headers - CRITICAL: Must include Authorization for JWT
        if ("*".equals(allowedHeaders)) {
            config.addAllowedHeader("*");
        } else {
            List<String> headers = Arrays.asList(allowedHeaders.split(","));
            for (String header : headers) {
                String trimmedHeader = header.trim();
                if (!trimmedHeader.isEmpty()) {
                    config.addAllowedHeader(trimmedHeader);
                }
            }
        }

        // Explicitly allow Authorization header (needed for JWT preflight requests)
        config.addAllowedHeader("Authorization");
        config.addAllowedHeader("Content-Type");
        config.addAllowedHeader("Accept");

        // Allow credentials (cookies, authorization headers) - needed for JWT
        config.setAllowCredentials(allowCredentials);
        
        // Cache preflight responses - prevents repeated OPTIONS calls
        config.setMaxAge(maxAge);

        // IMPORTANT: Allow Authorization header to be exposed in response
        config.addExposedHeader("Authorization");
        config.addExposedHeader("X-Total-Count");
        config.addExposedHeader("X-Page-Number");

        // Apply CORS configuration to all paths
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
