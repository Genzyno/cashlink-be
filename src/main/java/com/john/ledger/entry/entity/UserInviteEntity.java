package com.john.ledger.entry.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_invite", schema = "ledger", indexes = {
    @Index(name = "idx_user_invite_token", columnList = "token"),
    @Index(name = "idx_user_invite_email", columnList = "email"),
    @Index(name = "idx_user_invite_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInviteEntity {

    public enum InviteStatus { PENDING, ACCEPTED, REJECTED, EXPIRED }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "role_id")
    private UUID roleId;

    @Column(name = "token", nullable = false, unique = true, length = 255)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private InviteStatus status = InviteStatus.PENDING;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "invited_by_user_id")
    private UUID invitedByUserId;

    @CreationTimestamp
    @Column(name = "created_time", updatable = false)
    private LocalDateTime createdTime;
}
