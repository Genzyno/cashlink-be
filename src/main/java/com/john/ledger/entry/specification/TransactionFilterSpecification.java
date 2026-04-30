package com.john.ledger.entry.specification;

import com.john.ledger.common.enums.TransactionType;
import com.john.ledger.entry.entity.TransactionEntity;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class TransactionFilterSpecification {

    private TransactionFilterSpecification() {
    }

    public static Specification<TransactionEntity> filterBy(
            UUID bookId,
            LocalDate fromDate,
            LocalDate toDate,
            TransactionType transactionType,
            UUID categoryId,
            UUID paymentModeId,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            String searchTerm,
            UUID createdByUserId,
            UUID updatedByUserId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (bookId != null) {
                predicates.add(cb.equal(root.get("bookId"), bookId));
            }
            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("date"), fromDate));
            }
            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("date"), toDate));
            }
            if (transactionType != null) {
                predicates.add(cb.equal(root.get("transactionType"), transactionType));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("categoryId"), categoryId));
            }
            if (paymentModeId != null) {
                predicates.add(cb.equal(root.get("paymentModeId"), paymentModeId));
            }
            if (minAmount != null && minAmount.compareTo(BigDecimal.ZERO) >= 0) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), minAmount));
            }
            if (maxAmount != null && maxAmount.compareTo(BigDecimal.ZERO) >= 0) {
                predicates.add(cb.lessThanOrEqualTo(root.get("amount"), maxAmount));
            }
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("remarks")), "%" + searchTerm.trim().toLowerCase() + "%"));
            }
            if (createdByUserId != null) {
                predicates.add(cb.equal(root.get("createdByUserId"), createdByUserId));
            }
            if (updatedByUserId != null) {
                predicates.add(cb.equal(root.get("updatedByUserId"), updatedByUserId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Filter for export: multi-value fields use IN (empty/omit = no restriction).
     */
    public static Specification<TransactionEntity> filterByExport(
            UUID bookId,
            LocalDate fromDate,
            LocalDate toDate,
            List<TransactionType> transactionTypes,
            List<UUID> categoryIds,
            List<UUID> paymentModeIds,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            List<UUID> createdByIds,
            List<UUID> updatedByIds) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (bookId != null) {
                predicates.add(cb.equal(root.get("bookId"), bookId));
            }
            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("date"), fromDate));
            }
            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("date"), toDate));
            }
            if (transactionTypes != null && !transactionTypes.isEmpty()) {
                predicates.add(root.get("transactionType").in(transactionTypes));
            }
            if (categoryIds != null && !categoryIds.isEmpty()) {
                predicates.add(root.get("categoryId").in(categoryIds));
            }
            if (paymentModeIds != null && !paymentModeIds.isEmpty()) {
                predicates.add(root.get("paymentModeId").in(paymentModeIds));
            }
            if (minAmount != null && minAmount.compareTo(BigDecimal.ZERO) >= 0) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), minAmount));
            }
            if (maxAmount != null && maxAmount.compareTo(BigDecimal.ZERO) >= 0) {
                predicates.add(cb.lessThanOrEqualTo(root.get("amount"), maxAmount));
            }
            if (createdByIds != null && !createdByIds.isEmpty()) {
                predicates.add(root.get("createdByUserId").in(createdByIds));
            }
            if (updatedByIds != null && !updatedByIds.isEmpty()) {
                predicates.add(root.get("updatedByUserId").in(updatedByIds));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
