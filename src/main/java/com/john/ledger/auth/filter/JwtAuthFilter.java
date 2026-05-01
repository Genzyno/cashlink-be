package com.john.ledger.auth.filter;

import com.john.ledger.auth.service.JwtService;
import com.john.ledger.common.util.CurrentUserHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String uri = request.getRequestURI();

        return uri.contains("/auth/")
                || uri.contains("/public/")
                || uri.contains("/actuator")
                || uri.contains("/swagger-ui")
                || uri.contains("/v3/api-docs")
                || request.getMethod().equalsIgnoreCase("OPTIONS");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(401);
            response.getWriter().write("{\"message\":\"Missing token\"}");
            return;
        }

        String token = authHeader.substring(7);

        JwtService.TokenPayload payload = jwtService.parseAccessToken(token);

        if (payload == null) {
            response.setStatus(401);
            response.getWriter().write("{\"message\":\"Invalid token\"}");
            return;
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
}