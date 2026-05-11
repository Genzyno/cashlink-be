package com.john.ledger.entry.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    @Size(min = 3, max = 50)
    private String userName;

    @Column(nullable = false, unique = true, length = 100)
    private String userEmail;

    @Column(nullable = false, unique = true, length = 15)
    private String userMobile;

    @Column(nullable = false)
    private String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private RoleEntity roleEntity;

    @Column(name = "admin_id")
    private UUID adminId;

    @Builder.Default
    @Column(nullable = false)
    private Boolean status = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdTime;

    @UpdateTimestamp
    @Column
    private LocalDateTime updatedTime;
}
