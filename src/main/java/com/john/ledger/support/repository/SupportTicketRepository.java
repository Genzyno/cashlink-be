package com.john.ledger.support.repository;

import com.john.ledger.support.entity.SupportTicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicketEntity, UUID> {
    List<SupportTicketEntity> findByBusinessIdAndUserIdOrderByCreatedTimeDesc(UUID businessId, UUID userId);
    List<SupportTicketEntity> findByUserIdOrderByCreatedTimeDesc(UUID userId);
}
