package com.john.ledger.auth.filter;

import com.john.ledger.auth.service.JwtService;
import com.john.ledger.common.util.CurrentUserHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // ===============================
        // ALWAYS allow OPTIONS (CORS preflight)
        // ===============================
        if ("OPTIONS".equalsIgnoreCase(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        // ===============================
        // PUBLIC ENDPOINTS
        // ===============================
        if (isPublicPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // ===============================
        // Skip Auth Header Support
        // ===============================
        String skipAuth = request.getHeader("X-Skip-Auth");
        if ("true".equalsIgnoreCase(skipAuth)) {
            filterChain.doFilter(request, response);
            return;
        }

        // ===============================
        // READ TOKEN
        // ===============================
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            send401(response, "Missing Authorization token");
            return;
        }

        String token = authHeader.substring(7).trim();

        if (token.isBlank()) {
            send401(response, "Invalid Authorization token");
            return;
        }

        // ===============================
        // VALIDATE TOKEN
        // ===============================
        JwtService.TokenPayload payload = jwtService.parseAccessToken(token);

        if (payload == null) {
            send401(response, "Token expired or invalid");
            return;
        }

        // ===============================
        // VALIDATE USER HEADER
        // ===============================
        String loggedUserId = request.getHeader("X-Logged-User-Id");

        if (loggedUserId != null && !loggedUserId.isBlank()) {
            try {
                UUID headerUserId = UUID.fromString(loggedUserId.trim());

                if (!headerUserId.toString().equals(payload.userId())) {
                    send403(response, "User mismatch");
                    return;
                }

            } catch (Exception e) {
                send403(response, "Invalid X-Logged-User-Id");
                return;
            }
        }

        // ===============================
        // STORE USER CONTEXT
        // ===============================
        request.setAttribute("userId", payload.userId());
        request.setAttribute("userEmail", payload.email());

        CurrentUserHolder.setUserId(payload.userId());

        try {
            filterChain.doFilter(request, response);
        } finally {
            CurrentUserHolder.clear();
        }
    }

    // ===============================
    // PUBLIC URLS
    // ===============================
    private boolean isPublicPath(String path) {

        if (path == null) return false;

        return

                // AUTH
                path.startsWith("/myledger-api/auth/send-otp") ||
                        path.startsWith("/myledger-api/auth/verify-otp") ||
                        path.startsWith("/myledger-api/auth/refresh") ||
                        path.startsWith("/myledger-api/auth/logout") ||
                        path.startsWith("/myledger-api/auth/forgot-password") ||
                        path.startsWith("/myledger-api/auth/google") ||
                        path.startsWith("/myledger-api/auth/google/callback") ||
                        path.startsWith("/myledger-api/auth/send-verification-email") ||
                        path.startsWith("/myledger-api/auth/verify-email") ||

                        // PUBLIC
                        path.startsWith("/myledger-api/public/") ||

                        // SWAGGER
                        path.startsWith("/swagger-ui") ||
                        path.startsWith("/v3/api-docs") ||

                        // ACTUATOR
                        path.startsWith("/actuator") ||

                        // ERROR
                        path.startsWith("/error");
    }

    // ===============================
    // 401
    // ===============================
    private void send401(HttpServletResponse response, String msg) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        response.getWriter().write(
                "{\"statusCode\":401,\"message\":\"" +
                        escape(msg) +
                        "\",\"data\":null}"
        );
    }

    // ===============================
    // 403
    // ===============================
    private void send403(HttpServletResponse response, String msg) throws IOException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        response.getWriter().write(
                "{\"statusCode\":403,\"message\":\"" +
                        escape(msg) +
                        "\",\"data\":null}"
        );
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}