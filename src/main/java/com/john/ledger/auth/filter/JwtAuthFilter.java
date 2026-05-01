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

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String uri = request.getRequestURI();
        String method = request.getMethod();

        // Allow preflight
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        // Public endpoints
        return uri.contains("/auth/")
                || uri.contains("/public/")
                || uri.contains("/actuator")
                || uri.contains("/swagger-ui")
                || uri.contains("/v3/api-docs")
                || uri.contains("/swagger-resources")
                || uri.contains("/webjars");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {

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

            JwtService.TokenPayload payload = jwtService.parseAccessToken(token);

            if (payload == null) {
                send401(response, "Expired or invalid token");
                return;
            }

            // Set request attributes
            request.setAttribute("userId", payload.userId());
            request.setAttribute("userEmail", payload.email());

            // Thread local
            CurrentUserHolder.setUserId(payload.userId());

            filterChain.doFilter(request, response);

        } finally {
            CurrentUserHolder.clear();
        }
    }

    private void send401(HttpServletResponse response, String message) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        response.getWriter().write(
                "{\"message\":\"" + escapeJson(message) + "\",\"statusCode\":401,\"data\":null}"
        );
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}