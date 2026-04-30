package com.john.ledger.auth.repository;

import com.john.ledger.auth.entity.OtpEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpRepository extends JpaRepository<OtpEntity, UUID> {

    Optional<OtpEntity> findByEmailAndClientIdAndOtpCode(String email, String clientId, String otpCode);

    @Modifying
    @Query("DELETE FROM OtpEntity o WHERE o.email = :email AND o.clientId = :clientId AND o.otpCode = :otpCode")
    void deleteByEmailAndClientIdAndOtpCode(@Param("email") String email, @Param("clientId") String clientId, @Param("otpCode") String otpCode);

    @Query("SELECT COUNT(o) FROM OtpEntity o WHERE LOWER(o.email) = LOWER(:email) AND o.createdTime >= :since")
    long countByEmailSince(@Param("email") String email, @Param("since") LocalDateTime since);
}
