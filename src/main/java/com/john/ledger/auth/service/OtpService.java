package com.john.ledger.auth.service;

import com.john.ledger.config.AppProperties;
import com.john.ledger.auth.entity.OtpEntity;
import com.john.ledger.auth.repository.OtpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class OtpService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final int otpLength;
    private final int expirationMinutes;
    private final int rateLimitPerEmailPerMin;

    @Autowired
    private OtpRepository otpRepository;

    public OtpService(AppProperties appProperties) {
        this.otpLength = appProperties.getOtp().getLength() > 0 ? appProperties.getOtp().getLength() : 6;
        this.expirationMinutes = appProperties.getOtp().getExpirationMin() > 0 ? appProperties.getOtp().getExpirationMin() : 5;
        this.rateLimitPerEmailPerMin = appProperties.getOtp().getRateLimitPerEmailPerMin() > 0 ? appProperties.getOtp().getRateLimitPerEmailPerMin() : 2;
    }

    @Transactional
    public String generateAndStore(String email, String channel, String clientId) {
        String emailLower = email != null ? email.toLowerCase().trim() : "";
        if (emailLower.isEmpty() || clientId == null || clientId.isBlank()) return null;

        LocalDateTime since = LocalDateTime.now().minusMinutes(1);
        long count = otpRepository.countByEmailSince(emailLower, since);
        if (count >= rateLimitPerEmailPerMin) {
            return null;
        }

        String otp = generateNumericOtp(otpLength);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(expirationMinutes);

        OtpEntity entity = OtpEntity.builder()
                .email(emailLower)
                .otpCode(otp)
                .channel(channel != null ? channel : "WEB")
                .clientId(clientId)
                .expiresAt(expiresAt)
                .build();
        otpRepository.save(entity);
        return otp;
    }

    @Transactional
    public boolean consumeOtp(String email, String otp, String clientId) {
        if (email == null || email.isBlank() || otp == null || otp.isBlank()) return false;
        String emailLower = email.toLowerCase().trim();

        Optional<OtpEntity> opt = otpRepository.findByEmailAndClientIdAndOtpCode(emailLower, clientId != null ? clientId : "", otp);
        if (opt.isEmpty()) return false;

        OtpEntity entity = opt.get();
        if (entity.getExpiresAt().isBefore(LocalDateTime.now())) {
            otpRepository.delete(entity);
            return false;
        }

        otpRepository.delete(entity);
        return true;
    }

    private static String generateNumericOtp(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }
}
