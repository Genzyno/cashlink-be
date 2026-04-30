package com.john.ledger.entry.repository;

import com.john.ledger.common.enums.CategoryType;
import com.john.ledger.entry.entity.BookCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookCategoryRepository extends JpaRepository<BookCategoryEntity, UUID> {

    Optional<BookCategoryEntity> findByCategoryNameAndBusinessIdAndCategoryType(String categoryName, UUID businessId, CategoryType categoryType);

    List<BookCategoryEntity> findAllByBusinessId(UUID businessId);
}
