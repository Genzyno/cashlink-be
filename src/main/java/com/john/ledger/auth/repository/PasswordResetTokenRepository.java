package com.john.ledger.auth.repository;

import com.john.ledger.auth.entity.PasswordResetTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, UUID> {

    Optional<PasswordResetTokenEntity> findByToken(String token);

    @Query("SELECT COUNT(p) FROM PasswordResetTokenEntity p WHERE LOWER(p.email) = LOWER(:email) AND p.createdTime >= :since AND p.used = false")
    long countByEmailSince(@Param("email") String email, @Param("since") LocalDateTime since);

    @Modifying
    @Query("DELETE FROM PasswordResetTokenEntity p WHERE p.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
}
