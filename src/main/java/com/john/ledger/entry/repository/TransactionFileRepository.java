package com.john.ledger.entry.repository;

import com.john.ledger.entry.entity.TransactionFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TransactionFileRepository extends JpaRepository<TransactionFileEntity, UUID> {
}
