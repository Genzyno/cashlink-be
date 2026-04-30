package com.john.ledger.entry.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "business")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    @Size(min = 3, max = 50)
    private String businessName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_type_id", nullable = false)
    private BusinessTypeEntity businessTypeEntity;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private String financialYear;

    /** User who created the business (for scoping: show only own + assigned). Nullable for existing rows. */
    @Column(name = "created_by_user_id")
    private java.util.UUID createdByUserId;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdTime;

    @UpdateTimestamp
    @Column
    private LocalDateTime updatedTime;
}
