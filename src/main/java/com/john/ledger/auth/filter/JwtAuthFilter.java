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
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // ===============================
        // PUBLIC URLS
        // ===============================
        if ("OPTIONS".equalsIgnoreCase(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (path != null) {

            // auth urls
            if (path.startsWith("/myledger-api/auth/")
                    || path.startsWith("/auth/")) {
                filterChain.doFilter(request, response);
                return;
            }

            // public urls
            if (path.startsWith("/myledger-api/public/")
                    || path.startsWith("/public/")) {
                filterChain.doFilter(request, response);
                return;
            }

            // actuator
            if (path.startsWith("/actuator")) {
                filterChain.doFilter(request, response);
                return;
            }

            // swagger
            if (path.contains("/swagger-ui")
                    || path.contains("/v3/api-docs")) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        // ===============================
        // READ TOKEN
        // ===============================
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            send401(response, "Missing token");
            return;
        }

        String token = authHeader.substring(7).trim();

        if (token.isEmpty()) {
            send401(response, "Invalid token");
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
        // GET USER ID FROM TOKEN
        // ===============================
        String tokenUserId = payload.userId();

        if (tokenUserId == null || tokenUserId.isBlank()) {
            send401(response, "Invalid token user");
            return;
        }

        // ===============================
        // OPTIONAL HEADER CHECK
        // ===============================
        String headerUserId = request.getHeader("X-Logged-User-Id");

        if (headerUserId != null && !headerUserId.isBlank()) {

            try {
                UUID.fromString(headerUserId);

                if (!headerUserId.equals(tokenUserId)) {
                    send403(response, "User mismatch");
                    return;
                }

            } catch (Exception ex) {
                send403(response, "Invalid user id");
                return;
            }
        }

        // ===============================
        // SET USER DATA
        // ===============================
        request.setAttribute("userId", tokenUserId);
        request.setAttribute("userEmail", payload.email());

        CurrentUserHolder.setUserId(tokenUserId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            CurrentUserHolder.clear();
        }
    }

    // ===============================
    // 401
    // ===============================
    private void send401(HttpServletResponse response, String msg) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(
                "{\"message\":\"" + msg + "\",\"statusCode\":401}"
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
                "{\"message\":\"" + msg + "\",\"statusCode\":403}"
        );
    }
}