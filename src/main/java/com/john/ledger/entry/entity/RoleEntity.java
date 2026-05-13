package com.john.ledger.entry.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "role", schema = "ledger")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "role_name", nullable = false, unique = true, length = 100)
    @Size(min = 2, max = 100)
    private String roleName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "permissions", columnDefinition = "jsonb")
    private Map<String, List<String>> permissions;

    /** Screen/action scope: "all" or "assigned" per action. e.g. business -> { "view": "all", "create": "assigned" } */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "permission_scopes", columnDefinition = "jsonb")
    private Map<String, Map<String, String>> permissionScopes;

    @Column(name = "admin_id")
    private UUID adminId;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdTime;

    @UpdateTimestamp
    @Column
    private LocalDateTime updatedTime;
}
