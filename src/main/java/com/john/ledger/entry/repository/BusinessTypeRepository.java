package com.john.ledger.entry.repository;

import com.john.ledger.entry.entity.BusinessTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessTypeRepository extends JpaRepository<BusinessTypeEntity, java.util.UUID> {
    Optional<BusinessTypeEntity> findByBusinessType(String businessType);

    @Query("SELECT b FROM BusinessTypeEntity b WHERE b.adminId IS NULL OR b.adminId = :adminId ORDER BY b.businessType ASC")
    List<BusinessTypeEntity> findAllByAdminId(java.util.UUID adminId);

    Optional<BusinessTypeEntity> findByBusinessTypeAndAdminId(String businessType, java.util.UUID adminId);
}
