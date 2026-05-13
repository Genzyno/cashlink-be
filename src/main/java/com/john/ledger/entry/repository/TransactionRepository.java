package com.john.ledger.entry.repository;

import com.john.ledger.entry.entity.TransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, java.util.UUID>, JpaSpecificationExecutor<TransactionEntity> {

    Page<TransactionEntity> findAllByBookId(java.util.UUID bookId, Pageable pageable);

    /** Chronological order (newest first) for fetching, reversed locally for balance calc. */
    List<TransactionEntity> findAllByBookIdOrderByDateDescTimeDescCreatedTimeDescIdDesc(java.util.UUID bookId);

    @Query("SELECT t FROM TransactionEntity t WHERE t.bookId = :bookId AND " +
            "LOWER(t.remarks) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<TransactionEntity> searchTransactions(@Param("bookId") java.util.UUID bookId,
                                               @Param("searchTerm") String searchTerm,
                                               Pageable pageable);

    @Query(value = "SELECT " +
            "COALESCE(SUM(CASE WHEN transaction_type = 'CASH_IN' THEN amount ELSE 0 END), 0) AS total_cash_in, " +
            "COALESCE(SUM(CASE WHEN transaction_type = 'CASH_OUT' THEN amount ELSE 0 END), 0) AS total_cash_out " +
            "FROM ledger.transactions WHERE book_id = :bookId", nativeQuery = true)
    List<Object[]> getTransactionSummary(@Param("bookId") java.util.UUID bookId);

    /** Dashboard: summary totals for a business (all books). */
    @Query(value = "SELECT " +
            "COALESCE(SUM(CASE WHEN transaction_type = 'CASH_IN' THEN amount ELSE 0 END), 0) AS total_cash_in, " +
            "COALESCE(SUM(CASE WHEN transaction_type = 'CASH_OUT' THEN amount ELSE 0 END), 0) AS total_cash_out, " +
            "COUNT(*) AS total_transactions, " +
            "COUNT(DISTINCT book_id) AS total_books " +
            "FROM ledger.transactions WHERE business_id = :businessId", nativeQuery = true)
    List<Object[]> getBusinessSummary(@Param("businessId") java.util.UUID businessId);

    /** Dashboard: summary for a business within date range. */
    @Query(value = "SELECT " +
            "COALESCE(SUM(CASE WHEN transaction_type = 'CASH_IN' THEN amount ELSE 0 END), 0) AS total_cash_in, " +
            "COALESCE(SUM(CASE WHEN transaction_type = 'CASH_OUT' THEN amount ELSE 0 END), 0) AS total_cash_out, " +
            "COUNT(*) AS total_transactions, " +
            "COUNT(DISTINCT book_id) AS total_books " +
            "FROM ledger.transactions WHERE business_id = :businessId " +
            "AND transaction_date >= :fromDate AND transaction_date <= :toDate", nativeQuery = true)
    List<Object[]> getBusinessSummaryWithDateRange(@Param("businessId") java.util.UUID businessId,
                                                   @Param("fromDate") LocalDate fromDate,
                                                   @Param("toDate") LocalDate toDate);

    /** Dashboard: recent transactions for a business (any book), newest first. */
    List<TransactionEntity> findTop10ByBusinessIdOrderByDateDescTimeDescCreatedTimeDescIdDesc(java.util.UUID businessId);

    /** Dashboard: recent transactions for a business within date range, newest first. */
    List<TransactionEntity> findTop10ByBusinessIdAndDateBetweenOrderByDateDescTimeDescCreatedTimeDescIdDesc(
            java.util.UUID businessId, LocalDate fromDate, LocalDate toDate);

    /** Dashboard: monthly trend (net cash flow per month) for date range. Returns month label and net (cashIn - cashOut). */
    @Query(value = "SELECT TO_CHAR(transaction_date, 'Mon') AS x, " +
            "COALESCE(SUM(CASE WHEN transaction_type = 'CASH_IN' THEN amount ELSE 0 END), 0) - " +
            "COALESCE(SUM(CASE WHEN transaction_type = 'CASH_OUT' THEN amount ELSE 0 END), 0) AS y " +
            "FROM ledger.transactions " +
            "WHERE business_id = :businessId AND transaction_date >= :fromDate AND transaction_date <= :toDate " +
            "GROUP BY DATE_TRUNC('month', transaction_date), TO_CHAR(transaction_date, 'Mon') " +
            "ORDER BY DATE_TRUNC('month', transaction_date)", nativeQuery = true)
    List<Object[]> getTrendDataMonthly(@Param("businessId") java.util.UUID businessId,
                                       @Param("fromDate") LocalDate fromDate,
                                       @Param("toDate") LocalDate toDate);

    /** Dashboard: weekly trend (net cash flow per week). */
    @Query(value = "SELECT TO_CHAR(DATE_TRUNC('week', transaction_date)::date, 'Mon DD') AS x, " +
            "COALESCE(SUM(CASE WHEN transaction_type = 'CASH_IN' THEN amount ELSE 0 END), 0) - " +
            "COALESCE(SUM(CASE WHEN transaction_type = 'CASH_OUT' THEN amount ELSE 0 END), 0) AS y " +
            "FROM ledger.transactions " +
            "WHERE business_id = :businessId AND transaction_date >= :fromDate AND transaction_date <= :toDate " +
            "GROUP BY DATE_TRUNC('week', transaction_date) " +
            "ORDER BY DATE_TRUNC('week', transaction_date)", nativeQuery = true)
    List<Object[]> getTrendDataWeekly(@Param("businessId") java.util.UUID businessId,
                                      @Param("fromDate") LocalDate fromDate,
                                      @Param("toDate") LocalDate toDate);

    /** Dashboard: daily trend (net cash flow per day). */
    @Query(value = "SELECT TO_CHAR(transaction_date, 'Mon DD') AS x, " +
            "COALESCE(SUM(CASE WHEN transaction_type = 'CASH_IN' THEN amount ELSE 0 END), 0) - " +
            "COALESCE(SUM(CASE WHEN transaction_type = 'CASH_OUT' THEN amount ELSE 0 END), 0) AS y " +
            "FROM ledger.transactions " +
            "WHERE business_id = :businessId AND transaction_date >= :fromDate AND transaction_date <= :toDate " +
            "GROUP BY transaction_date " +
            "ORDER BY transaction_date", nativeQuery = true)
    List<Object[]> getTrendDataDaily(@Param("businessId") java.util.UUID businessId,
                                     @Param("fromDate") LocalDate fromDate,
                                     @Param("toDate") LocalDate toDate);

    // ---------- Analytics ----------

    /** Analytics: overview totals. When bookId is null, filter by businessId only. */
    @Query(value = "SELECT " +
            "COALESCE(SUM(CASE WHEN t.transaction_type = 'CASH_IN' THEN t.amount ELSE 0 END), 0), " +
            "COALESCE(SUM(CASE WHEN t.transaction_type = 'CASH_OUT' THEN t.amount ELSE 0 END), 0), " +
            "COUNT(*) " +
            "FROM ledger.transactions t " +
            "WHERE (cast(:businessId as uuid) IS NULL OR t.business_id = cast(:businessId as uuid)) " +
            "AND (cast(:bookId as uuid) IS NULL OR t.book_id = cast(:bookId as uuid)) " +
            "AND t.transaction_date >= :fromDate AND t.transaction_date <= :toDate", nativeQuery = true)
    List<Object[]> getAnalyticsOverview(@Param("businessId") java.util.UUID businessId, @Param("bookId") java.util.UUID bookId,
                                        @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    /** Analytics: category-wise. COALESCE category_id to handle nulls. */
    @Query(value = "SELECT COALESCE(t.category_id::text, '') AS cat_id, COALESCE(c.category_name, 'Uncategorized') AS cat_name, " +
            "t.transaction_type, SUM(t.amount) AS total, COUNT(*) AS cnt " +
            "FROM ledger.transactions t " +
            "LEFT JOIN ledger.categories c ON c.id = t.category_id " +
            "WHERE (cast(:businessId as uuid) IS NULL OR t.business_id = cast(:businessId as uuid)) " +
            "AND (cast(:bookId as uuid) IS NULL OR t.book_id = cast(:bookId as uuid)) " +
            "AND t.transaction_date >= :fromDate AND t.transaction_date <= :toDate " +
            "GROUP BY t.category_id, c.category_name, t.transaction_type ORDER BY total DESC", nativeQuery = true)
    List<Object[]> getAnalyticsCategoryWise(@Param("businessId") java.util.UUID businessId, @Param("bookId") java.util.UUID bookId,
                                            @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    /** Analytics: month-wise. */
    @Query(value = "SELECT EXTRACT(YEAR FROM transaction_date)::int AS y, EXTRACT(MONTH FROM transaction_date)::int AS m, " +
            "COALESCE(SUM(CASE WHEN transaction_type = 'CASH_IN' THEN amount ELSE 0 END), 0), " +
            "COALESCE(SUM(CASE WHEN transaction_type = 'CASH_OUT' THEN amount ELSE 0 END), 0), COUNT(*) " +
            "FROM ledger.transactions " +
            "WHERE (cast(:businessId as uuid) IS NULL OR business_id = cast(:businessId as uuid)) " +
            "AND (cast(:bookId as uuid) IS NULL OR book_id = cast(:bookId as uuid)) " +
            "AND transaction_date >= :fromDate AND transaction_date <= :toDate " +
            "GROUP BY EXTRACT(YEAR FROM transaction_date), EXTRACT(MONTH FROM transaction_date) " +
            "ORDER BY y, m", nativeQuery = true)
    List<Object[]> getAnalyticsMonthWise(@Param("businessId") java.util.UUID businessId, @Param("bookId") java.util.UUID bookId,
                                         @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    /** Analytics: business-wise (all businesses in date range). */
    @Query(value = "SELECT t.business_id, b.business_name, " +
            "COALESCE(SUM(CASE WHEN t.transaction_type = 'CASH_IN' THEN t.amount ELSE 0 END), 0), " +
            "COALESCE(SUM(CASE WHEN t.transaction_type = 'CASH_OUT' THEN t.amount ELSE 0 END), 0), " +
            "COUNT(*), COUNT(DISTINCT t.book_id) " +
            "FROM ledger.transactions t " +
            "JOIN ledger.business b ON b.id = t.business_id " +
            "WHERE t.transaction_date >= :fromDate AND t.transaction_date <= :toDate " +
            "GROUP BY t.business_id, b.business_name ORDER BY 3 DESC", nativeQuery = true)
    List<Object[]> getAnalyticsBusinessWise(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    /** Analytics: time-series by day. */
    @Query(value = "SELECT transaction_date::text, " +
            "COALESCE(SUM(CASE WHEN transaction_type = 'CASH_IN' THEN amount ELSE 0 END), 0), " +
            "COALESCE(SUM(CASE WHEN transaction_type = 'CASH_OUT' THEN amount ELSE 0 END), 0), COUNT(*) " +
            "FROM ledger.transactions " +
            "WHERE (cast(:businessId as uuid) IS NULL OR business_id = cast(:businessId as uuid)) " +
            "AND (cast(:bookId as uuid) IS NULL OR book_id = cast(:bookId as uuid)) " +
            "AND transaction_date >= :fromDate AND transaction_date <= :toDate " +
            "GROUP BY transaction_date ORDER BY transaction_date", nativeQuery = true)
    List<Object[]> getAnalyticsByDay(@Param("businessId") java.util.UUID businessId, @Param("bookId") java.util.UUID bookId,
                                     @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    /** Analytics: time-series by year. */
    @Query(value = "SELECT EXTRACT(YEAR FROM transaction_date)::int, " +
            "COALESCE(SUM(CASE WHEN transaction_type = 'CASH_IN' THEN amount ELSE 0 END), 0), " +
            "COALESCE(SUM(CASE WHEN transaction_type = 'CASH_OUT' THEN amount ELSE 0 END), 0), COUNT(*) " +
            "FROM ledger.transactions " +
            "WHERE (cast(:businessId as uuid) IS NULL OR business_id = cast(:businessId as uuid)) " +
            "AND (cast(:bookId as uuid) IS NULL OR book_id = cast(:bookId as uuid)) " +
            "AND transaction_date >= :fromDate AND transaction_date <= :toDate " +
            "GROUP BY EXTRACT(YEAR FROM transaction_date) ORDER BY 1", nativeQuery = true)
    List<Object[]> getAnalyticsByYear(@Param("businessId") java.util.UUID businessId, @Param("bookId") java.util.UUID bookId,
                                      @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);
}

