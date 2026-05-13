package com.john.ledger.entry.repository;

import com.john.ledger.common.enums.CategoryType;
import com.john.ledger.entry.entity.BookCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookCategoryRepository extends JpaRepository<BookCategoryEntity, java.util.UUID> {

    Optional<BookCategoryEntity> findByCategoryNameAndBusinessIdAndCategoryType(String categoryName, java.util.UUID businessId, CategoryType categoryType);

    @Query("SELECT c FROM BookCategoryEntity c WHERE c.adminId IS NULL OR c.adminId = :adminId ORDER BY c.categoryName ASC")
    List<BookCategoryEntity> findAllByAdminId(java.util.UUID adminId);

    List<BookCategoryEntity> findAllByBusinessId(java.util.UUID businessId);

    Optional<BookCategoryEntity> findByCategoryNameAndAdminId(String categoryName, java.util.UUID adminId);
}
