package com.john.ledger.entry.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "user_invite_book", schema = "ledger",
    uniqueConstraints = @UniqueConstraint(columnNames = {"invite_id", "book_id"}),
    indexes = @Index(name = "idx_user_invite_book_invite_id", columnList = "invite_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInviteBookEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "invite_id", nullable = false)
    private UUID inviteId;

    @Column(name = "book_id", nullable = false)
    private UUID bookId;
}
