package com.john.ledger.entry.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "book_type", schema = "ledger")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    @Size(min = 3, max = 50)
    private String bookType;
}
