package com.john.ledger.entry.repository;

import com.john.ledger.entry.entity.BusinessTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusinessTypeRepository extends JpaRepository<BusinessTypeEntity, UUID> {
    Optional<BusinessTypeEntity> findByBusinessType(String businessType);
}
