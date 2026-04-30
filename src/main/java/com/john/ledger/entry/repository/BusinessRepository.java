package com.john.ledger.entry.repository;

import com.john.ledger.entry.entity.BusinessEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusinessRepository extends JpaRepository<BusinessEntity, UUID> {
    Optional<BusinessEntity> findByBusinessName(String businessName);

    long countByBusinessTypeEntity_Id(UUID businessTypeId);


    @Query("""
        SELECT u FROM BusinessEntity u
        WHERE
        LOWER(u.businessName) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(u.currency) LIKE LOWER(CONCAT('%', :search, '%'))
        OR u.financialYear LIKE CONCAT('%', :search, '%')
    """)
    Page<BusinessEntity> searchBusiness(@Param("search") String search, Pageable pageable);

    /** Businesses that have at least one book assigned to the given user (for scope "assigned"). */
    @Query("SELECT be FROM BusinessEntity be WHERE be.id IN (SELECT b.businessId FROM BookEntity b JOIN b.assignedUsers u WHERE u.id = :userId)")
    Page<BusinessEntity> findByAssignedUserId(@Param("userId") UUID userId, Pageable pageable);

    /** Businesses created by the user OR with at least one book assigned to the user (user-scoped list, no demo/others). */
    @Query("SELECT be FROM BusinessEntity be WHERE be.createdByUserId = :userId OR be.id IN (SELECT b.businessId FROM BookEntity b JOIN b.assignedUsers u WHERE u.id = :userId)")
    Page<BusinessEntity> findByCreatedByUserIdOrAssignedUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("""
        SELECT be FROM BusinessEntity be WHERE (be.createdByUserId = :userId OR be.id IN (SELECT b.businessId FROM BookEntity b JOIN b.assignedUsers u WHERE u.id = :userId))
        AND (LOWER(be.businessName) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(be.currency) LIKE LOWER(CONCAT('%', :search, '%'))
        OR be.financialYear LIKE CONCAT('%', :search, '%'))
        """)
    Page<BusinessEntity> findByCreatedByUserIdOrAssignedUserIdAndSearch(@Param("userId") UUID userId, @Param("search") String search, Pageable pageable);
}
