package com.john.ledger.auth.repository;

import com.john.ledger.auth.entity.EmailVerificationTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationTokenEntity, UUID> {

    Optional<EmailVerificationTokenEntity> findByToken(String token);

    Optional<EmailVerificationTokenEntity> findByEmailAndVerifiedFalse(String email);

    @Query("SELECT e FROM EmailVerificationTokenEntity e WHERE LOWER(e.email) = LOWER(:email) AND e.verified = true ORDER BY e.verifiedAt DESC")
    Optional<EmailVerificationTokenEntity> findVerifiedByEmail(@Param("email") String email);

    @Modifying
    @Query("UPDATE EmailVerificationTokenEntity e SET e.verified = true, e.verifiedAt = :verifiedAt WHERE e.token = :token")
    int markAsVerified(@Param("token") String token, @Param("verifiedAt") LocalDateTime verifiedAt);

    @Query("SELECT COUNT(e) FROM EmailVerificationTokenEntity e WHERE LOWER(e.email) = LOWER(:email) AND e.createdTime >= :since AND e.verified = false")
    long countUnverifiedByEmailSince(@Param("email") String email, @Param("since") LocalDateTime since);

    @Modifying
    @Query("DELETE FROM EmailVerificationTokenEntity e WHERE e.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
}
