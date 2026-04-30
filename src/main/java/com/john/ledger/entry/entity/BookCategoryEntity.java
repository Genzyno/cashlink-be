package com.john.ledger.entry.entity;

import com.john.ledger.common.enums.CategoryType;
import com.john.ledger.common.enums.Status;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "categories", uniqueConstraints = {@UniqueConstraint(columnNames = {"business_id", "category_name"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookCategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_type", nullable = false, length = 10)
    private CategoryType categoryType;

    @Column(name = "color_code", length = 7)
    private String colorCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Status status;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdTime;

    @UpdateTimestamp
    @Column
    private LocalDateTime updatedTime;
}
