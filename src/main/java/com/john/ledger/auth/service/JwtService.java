package com.john.ledger.auth.service;

import com.john.ledger.auth.dto.LedgerAuthUserInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import com.john.ledger.config.AppProperties;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";
    private static final String CLAIM_TYPE = "type";
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_NAME = "name";

    private final SecretKey secretKey;
    private final long accessExpirationSec;
    private final long refreshExpirationSec;

    public JwtService(AppProperties appProperties) {
        String secret = appProperties.getJwt().getSecret();
        if (secret == null || secret.isBlank()) {
            secret = "your-256-bit-secret-change-in-production-make-it-long-enough";
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpirationSec = appProperties.getJwt().getAccessExpirationSec() > 0 
                ? appProperties.getJwt().getAccessExpirationSec() : 3600;
        this.refreshExpirationSec = appProperties.getJwt().getRefreshExpirationSec() > 0 
                ? appProperties.getJwt().getRefreshExpirationSec() : 604800;
    }

    public String createAccessToken(String userId, String email, String name) {
        return Jwts.builder()
                .subject(userId)
                .claim(CLAIM_TYPE, TYPE_ACCESS)
                .claim(CLAIM_EMAIL, email)
                .claim(CLAIM_NAME, name != null ? name : "")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessExpirationSec * 1000))
                .signWith(secretKey)
                .compact();
    }

    public String createRefreshToken(String userId, String email) {
        return Jwts.builder()
                .subject(userId)
                .claim(CLAIM_TYPE, TYPE_REFRESH)
                .claim(CLAIM_EMAIL, email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpirationSec * 1000))
                .signWith(secretKey)
                .compact();
    }

    public TokenPayload parseAccessToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) return null;
        if (!TYPE_ACCESS.equals(claims.get(CLAIM_TYPE, String.class))) return null;
        return new TokenPayload(
                claims.getSubject(),
                claims.get(CLAIM_EMAIL, String.class),
                claims.get(CLAIM_NAME, String.class)
        );
    }

    public TokenPayload parseRefreshToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) return null;
        if (!TYPE_REFRESH.equals(claims.get(CLAIM_TYPE, String.class))) return null;
        return new TokenPayload(
                claims.getSubject(),
                claims.get(CLAIM_EMAIL, String.class),
                null
        );
    }

    private Claims parseToken(String token) {
        if (token == null || token.isBlank()) return null;
        try {
            return Jwts.parser().verifyWith(secretKey).build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            return null;
        }
    }

    public int getAccessExpirationSec() {
        return (int) accessExpirationSec;
    }

    public record TokenPayload(String userId, String email, String name) {
        public LedgerAuthUserInfo toUserInfo() {
            return LedgerAuthUserInfo.builder()
                    .id(userId)
                    .email(email)
                    .name(name != null ? name : "")
                    .build();
        }
    }
}
