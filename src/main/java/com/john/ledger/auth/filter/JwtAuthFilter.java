package com.john.ledger.auth.filter;

import com.john.ledger.auth.service.JwtService;
import com.john.ledger.common.util.CurrentUserHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        // Skip JWT authentication for public endpoints
        if (path != null) {
            // Allow auth endpoints that do not require a token (login, refresh, logout, forgot-password, Google, verification email)
            if (path.contains("/auth/send-otp") || path.contains("/auth/verify-otp") || path.contains("/auth/refresh")
                    || path.contains("/auth/logout") || path.contains("/auth/forgot-password") || path.contains("/auth/google")
                    || path.contains("/auth/send-verification-email") || path.contains("/auth/verify-email")) {
                filterChain.doFilter(request, response);
                return;
            }
            // Public connectivity check (Android / Flutter can call before login)
            if (path.contains("/public/ping")) {
                filterChain.doFilter(request, response);
                return;
            }
            // Allow Swagger UI and API docs
            if (path.contains("/swagger-ui") ||
                path.contains("/swagger-ui/") ||
                path.contains("/v3/api-docs") ||
                path.contains("/api-docs")) {
                filterChain.doFilter(request, response);
                return;
            }
            // Allow invite-by-token (GET), accept-invite (POST), reject-invite (POST) for invitees without auth
            if (path.contains("/users/invite-by-token") || path.contains("/users/accept-invite") || path.contains("/users/reject-invite")) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        // Allow CORS preflight requests without authentication
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Allow requests with X-Skip-Auth header
        if ("true".equalsIgnoreCase(request.getHeader("X-Skip-Auth"))) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7).trim();
        }

        if (token == null || token.isEmpty()) {
            send401(response, "Missing or invalid token. Send Authorization: Bearer <token> or log in again.");
            return;
        }

        JwtService.TokenPayload payload = jwtService.parseAccessToken(token);
        if (payload == null) {
            send401(response, "Token expired or invalid. Log in again or refresh token.");
            return;
        }

        // When frontend sends X-Logged-User-Id, validate it matches the JWT (user-based filtering uses JWT userId)
        String loggedUserIdHeader = request.getHeader("X-Logged-User-Id");
        if (loggedUserIdHeader != null && !loggedUserIdHeader.isBlank()) {
            try {
                UUID headerUserId = UUID.fromString(loggedUserIdHeader.trim());
                if (!headerUserId.toString().equals(payload.userId())) {
                    send403(response, "X-Logged-User-Id does not match token");
                    return;
                }
            } catch (IllegalArgumentException e) {
                send403(response, "Invalid X-Logged-User-Id");
                return;
            }
        }

        request.setAttribute("userId", payload.userId());
        request.setAttribute("userEmail", payload.email());
        CurrentUserHolder.setUserId(payload.userId());
        try {
            filterChain.doFilter(request, response);
        } finally {
            CurrentUserHolder.clear();
        }
    }

    private void send401(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"message\":\"" + escapeJson(message) + "\",\"statusCode\":401,\"data\":null}");
    }

    private void send403(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"message\":\"" + escapeJson(message) + "\",\"statusCode\":403,\"data\":null}");
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
