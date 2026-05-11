package com.john.ledger.entry.repository;

import com.john.ledger.entry.entity.BookTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import java.util.UUID;

public interface BookTypeRepository extends JpaRepository<BookTypeEntity, UUID> {
    Optional<BookTypeEntity> findByBookType(String bookType);
}

