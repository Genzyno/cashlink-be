package com.john.ledger.entry.repository;

import com.john.ledger.entry.entity.BookEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<BookEntity, UUID> {

    Optional<BookEntity> findByBookNameAndBusinessId(String bookName, UUID businessId);

    @Query("SELECT b FROM BookEntity b WHERE b.businessId = :businessId ORDER BY b.createdTime DESC")
    Page<BookEntity> findByBusinessId(@Param("businessId") UUID businessId, Pageable pageable);

    @Query("SELECT b FROM BookEntity b WHERE b.businessId = :businessId AND LOWER(b.bookName) LIKE LOWER(CONCAT('%', :search, '%')) ORDER BY b.createdTime DESC")
    Page<BookEntity> findByBusinessIdAndBookNameContainingIgnoreCase(@Param("businessId") UUID businessId, @Param("search") String search, Pageable pageable);

    @Query("SELECT b FROM BookEntity b JOIN b.assignedUsers u WHERE u.id = :userId")
    List<BookEntity> findByAssignedUserId(@Param("userId") UUID userId);

    @Query("SELECT b FROM BookEntity b JOIN b.assignedUsers u WHERE b.businessId = :businessId AND u.id = :userId ORDER BY b.createdTime DESC")
    Page<BookEntity> findByBusinessIdAndAssignedUserId(@Param("businessId") UUID businessId, @Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT b FROM BookEntity b JOIN b.assignedUsers u WHERE b.businessId = :businessId AND u.id = :userId AND LOWER(b.bookName) LIKE LOWER(CONCAT('%', :search, '%')) ORDER BY b.createdTime DESC")
    Page<BookEntity> findByBusinessIdAndAssignedUserIdAndBookNameContaining(@Param("businessId") UUID businessId, @Param("userId") UUID userId, @Param("search") String search, Pageable pageable);

    @Query("SELECT DISTINCT b.businessId FROM BookEntity b JOIN b.assignedUsers u WHERE u.id = :userId")
    List<UUID> findBusinessIdsByAssignedUserId(@Param("userId") UUID userId);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM BookEntity b JOIN b.assignedUsers u WHERE b.id = :bookId AND u.id = :userId")
    boolean existsByIdAndAssignedUserId(@Param("bookId") UUID bookId, @Param("userId") UUID userId);

    /** User IDs that share at least one book with the given user (for team "assigned" scope). */
    @Query("SELECT DISTINCT u.id FROM BookEntity b JOIN b.assignedUsers u WHERE b IN (SELECT b2 FROM BookEntity b2 JOIN b2.assignedUsers a WHERE a.id = :userId)")
    List<UUID> findUserIdsSharingBookWith(@Param("userId") UUID userId);
}
