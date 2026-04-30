package com.john.ledger.auth.service;

import com.john.ledger.auth.entity.EmailVerificationTokenEntity;
import com.john.ledger.auth.repository.EmailVerificationTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
public class EmailVerificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailVerificationService.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final int expirationHours;
    private final int rateLimitPerEmailPerHour;

    @Autowired
    private EmailVerificationTokenRepository tokenRepository;

    @Autowired
    private EmailVerificationEmailService emailService;

    public EmailVerificationService(
            @Value("${app.email-verification.expiration-hours:24}") int expirationHours,
            @Value("${app.email-verification.rate-limit-per-email-per-hour:3}") int rateLimitPerEmailPerHour) {
        this.expirationHours = expirationHours;
        this.rateLimitPerEmailPerHour = rateLimitPerEmailPerHour;
    }

    @Transactional
    public boolean generateAndSendVerificationToken(String email, String frontendUrl) {
        if (email == null || email.isBlank()) {
            log.warn("Email is null or blank");
            return false;
        }

        String emailLower = email.toLowerCase().trim();

        // Rate limiting: check if too many tokens sent in the last hour
        LocalDateTime since = LocalDateTime.now().minusHours(1);
        long count = tokenRepository.countUnverifiedByEmailSince(emailLower, since);
        if (count >= rateLimitPerEmailPerHour) {
            log.warn("Rate limit exceeded for email: {}", emailLower);
            return false;
        }

        // Invalidate any existing unverified tokens for this email
        Optional<EmailVerificationTokenEntity> existingTokenOpt = tokenRepository.findByEmailAndVerifiedFalse(emailLower);
        if (existingTokenOpt.isPresent()) {
            tokenRepository.delete(existingTokenOpt.get());
        }

        // Generate secure token
        String token = generateSecureToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(expirationHours);

        // Save token
        EmailVerificationTokenEntity tokenEntity = EmailVerificationTokenEntity.builder()
                .email(emailLower)
                .token(token)
                .expiresAt(expiresAt)
                .verified(false)
                .build();
        tokenRepository.save(tokenEntity);

        // Send email asynchronously
        emailService.sendVerificationEmail(emailLower, token, frontendUrl);

        log.info("Verification token generated and email queued for: {}", emailLower);
        return true;
    }

    @Transactional
    public boolean verifyToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        Optional<EmailVerificationTokenEntity> tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isEmpty()) {
            log.warn("Token not found: {}", token);
            return false;
        }

        EmailVerificationTokenEntity tokenEntity = tokenOpt.get();

        // Check if already verified - if so, return true (already verified)
        if (Boolean.TRUE.equals(tokenEntity.getVerified())) {
            log.info("Token already verified: {}", token);
            return true;
        }

        // Check if expired
        if (tokenEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("Token expired: {}", token);
            tokenRepository.delete(tokenEntity);
            return false;
        }

        // Mark as verified
        LocalDateTime now = LocalDateTime.now();
        int updated = tokenRepository.markAsVerified(token, now);
        
        if (updated > 0) {
            log.info("Email verified successfully: {}", tokenEntity.getEmail());
            return true;
        }

        return false;
    }

    public boolean isEmailVerified(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        String emailLower = email.toLowerCase().trim();
        // Check if there's a verified token for this email
        Optional<EmailVerificationTokenEntity> verifiedTokenOpt = tokenRepository.findVerifiedByEmail(emailLower);
        return verifiedTokenOpt.isPresent();
    }

    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
