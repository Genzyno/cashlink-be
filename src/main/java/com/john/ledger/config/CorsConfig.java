package com.john.ledger.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Autowired
    private AppProperties appProperties;

    /**
     * CORS ConfigurationSource Bean - Returns the CORS configuration for all
     * requests.
     * Used by Spring Security's SecurityFilterChain to properly handle CORS.
     * Allows specified origins and includes Authorization header in preflight
     * responses.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Configure allowed origins
        String allowedOrigins = appProperties.getCors().getAllowedOrigins();
        String originsTrimmed = allowedOrigins != null ? allowedOrigins.trim() : "";
        if (originsTrimmed.isEmpty() || "*".equals(originsTrimmed)) {
            config.addAllowedOriginPattern("*");
        } else {
            List<String> origins = Arrays.asList(allowedOrigins.split(","));
            for (String origin : origins) {
                String trimmedOrigin = origin.trim();
                if (!trimmedOrigin.isEmpty()) {
                    if ("*".equals(trimmedOrigin)) {
                        config.addAllowedOriginPattern("*");
                    } else {
                        config.addAllowedOrigin(trimmedOrigin);
                    }
                }
            }
            // Always allow localhost in dev
            config.addAllowedOrigin("http://localhost:4200");
            config.addAllowedOrigin("http://127.0.0.1:4200");
        }

        // Parse allowed methods
        String allowedMethods = appProperties.getCors().getAllowedMethods();
        if (allowedMethods == null || allowedMethods.isBlank()) {
            allowedMethods = "GET,POST,PUT,DELETE,OPTIONS,PATCH";
        }
        List<String> methods = Arrays.asList(allowedMethods.split(","));
        for (String method : methods) {
            String trimmedMethod = method.trim();
            if (!trimmedMethod.isEmpty()) {
                config.addAllowedMethod(trimmedMethod);
            }
        }

        // Parse allowed headers - CRITICAL: Must include Authorization for JWT
        String allowedHeaders = appProperties.getCors().getAllowedHeaders();
        if ("*".equals(allowedHeaders) || allowedHeaders == null || allowedHeaders.isBlank()) {
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
        config.setAllowCredentials(appProperties.getCors().isAllowCredentials());

        // Cache preflight responses - prevents repeated OPTIONS calls
        long maxAge = appProperties.getCors().getMaxAge();
        config.setMaxAge(maxAge > 0 ? maxAge : 3600);

        // IMPORTANT: Allow Authorization header to be exposed in response
        config.addExposedHeader("Authorization");
        config.addExposedHeader("X-Total-Count");
        config.addExposedHeader("X-Page-Number");

        // Apply CORS configuration to all paths
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
