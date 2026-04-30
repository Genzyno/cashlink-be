package com.john.ledger.entry.repository;

import com.john.ledger.entry.entity.PaymentModeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentModeRepository extends JpaRepository<PaymentModeEntity, UUID> {

    Optional<PaymentModeEntity> findByPaymentModeNameAndBusinessId(String paymentModeName, UUID businessId);

    List<PaymentModeEntity> findAllByBusinessId(UUID businessId);
}
