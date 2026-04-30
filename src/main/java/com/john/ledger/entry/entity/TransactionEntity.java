package com.john.ledger.entry.entity;

import com.john.ledger.common.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_transaction_book_id", columnList = "book_id"),
        @Index(name = "idx_transaction_created_by", columnList = "created_by_user_id"),
        @Index(name = "idx_transaction_updated_by", columnList = "updated_by_user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @Column(name = "book_id", nullable = false)
    private UUID bookId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 10)
    private TransactionType transactionType;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate date;

    /** Transaction time with millisecond precision (PostgreSQL TIME stores fractional seconds). */
    @Column(name = "transaction_time", nullable = false)
    private LocalTime time;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(length = 500)
    private String remarks;

    @Column(name = "category_id")
    private UUID categoryId;

    @Column(name = "payment_mode_id")
    private UUID paymentModeId;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<TransactionFileEntity> billFiles = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdTime;

    @UpdateTimestamp
    @Column
    private LocalDateTime updatedTime;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @Column(name = "updated_by_user_id")
    private UUID updatedByUserId;
}
