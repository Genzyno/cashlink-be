package com.john.ledger.entry.service.impl;

import com.john.ledger.common.enums.TransactionType;
import com.john.ledger.common.util.ResponseMessages;
import com.john.ledger.common.util.ServiceResponse;
import com.john.ledger.entry.dto.response.*;
import com.john.ledger.entry.entity.BookCategoryEntity;
import com.john.ledger.entry.entity.BookEntity;
import com.john.ledger.entry.entity.BusinessEntity;
import com.john.ledger.entry.entity.TransactionEntity;
import com.john.ledger.entry.repository.BookCategoryRepository;
import com.john.ledger.entry.repository.BookRepository;
import com.john.ledger.entry.repository.BusinessRepository;
import com.john.ledger.entry.repository.TransactionRepository;
import com.john.ledger.entry.util.PermissionScopeHelper;
import com.john.ledger.entry.service.IDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DashboardService implements IDashboardService {

    @Autowired
    private BusinessRepository businessRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private BookCategoryRepository bookCategoryRepository;
    @Autowired
    private PermissionScopeHelper permissionScopeHelper;

    @Override
    public ServiceResponse<DashboardOverviewResponse> getOverview(UUID businessId, LocalDate fromDate, LocalDate toDate) {
        try {
            if (businessId == null) {
                return ServiceResponse.failureResponse(400, "Business ID is required");
            }
            if (permissionScopeHelper.isAssignedScope("dashboard", "view") && permissionScopeHelper.getCurrentUserId().isPresent()) {
                List<UUID> allowed = bookRepository.findBusinessIdsByAssignedUserId(permissionScopeHelper.getCurrentUserId().get());
                if (!allowed.contains(businessId)) {
                    return ServiceResponse.failureResponse(403, "Access denied: you can only view dashboard for assigned businesses.");
                }
            }
            Optional<BusinessEntity> businessOpt = businessRepository.findById(businessId);
            if (businessOpt.isEmpty()) {
                return ServiceResponse.failureResponse(404, "Business not found");
            }

            BusinessEntity business = businessOpt.get();
            List<Object[]> summaryRows = transactionRepository.getBusinessSummaryWithDateRange(businessId, fromDate, toDate);
            long categoryCount = bookCategoryRepository.findAllByBusinessId(businessId).size();

            BigDecimal totalCashIn = BigDecimal.ZERO;
            BigDecimal totalCashOut = BigDecimal.ZERO;
            long totalTransactions = 0;
            long totalBooks = 0;

            if (summaryRows != null && !summaryRows.isEmpty()) {
                Object[] row = summaryRows.get(0);
                totalCashIn = new BigDecimal(row[0].toString());
                totalCashOut = new BigDecimal(row[1].toString());
                totalTransactions = row[2] != null ? Long.parseLong(row[2].toString()) : 0;
                totalBooks = row[3] != null ? Long.parseLong(row[3].toString()) : 0;
            }

            BigDecimal netBalance = totalCashIn.subtract(totalCashOut);

            DashboardSummaryDTO summary = DashboardSummaryDTO.builder()
                    .totalCashIn(totalCashIn)
                    .totalCashOut(totalCashOut)
                    .netBalance(netBalance)
                    .totalTransactions(totalTransactions)
                    .totalBooks(totalBooks)
                    .totalCategories(categoryCount)
                    .build();

            DashboardOverviewDTO overview = DashboardOverviewDTO.builder()
                    .businessName(business.getBusinessName())
                    .bookCount(totalBooks)
                    .categoryCount(categoryCount)
                    .build();

            List<TransactionEntity> recentList = transactionRepository.findTop10ByBusinessIdAndDateBetweenOrderByDateDescTimeDescCreatedTimeDescIdDesc(
                    businessId, fromDate, toDate);
            List<RecentTransactionItemDTO> recentTransactions = new ArrayList<>();
            for (TransactionEntity t : recentList) {
                String categoryName = t.getCategoryId() != null
                        ? bookCategoryRepository.findById(t.getCategoryId()).map(BookCategoryEntity::getCategoryName).orElse(null)
                        : null;
                String bookName = bookRepository.findById(t.getBookId()).map(BookEntity::getBookName).orElse(null);
                recentTransactions.add(RecentTransactionItemDTO.builder()
                        .id(t.getId())
                        .date(t.getDate())
                        .time(t.getTime())
                        .remarks(t.getRemarks())
                        .amount(t.getAmount())
                        .type(t.getTransactionType() == TransactionType.CASH_IN ? "CASH_IN" : "CASH_OUT")
                        .categoryName(categoryName)
                        .bookName(bookName)
                        .build());
            }

            List<TrendDataItemDTO> trendData = buildTrendData(businessId, fromDate, toDate, "month");

            DashboardOverviewResponse data = DashboardOverviewResponse.builder()
                    .summary(summary)
                    .overview(overview)
                    .recentTransactions(recentTransactions)
                    .trendData(trendData != null ? trendData : List.of())
                    .build();

            return ServiceResponse.successResponse(200, "Success", data);
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

    @Override
    public ServiceResponse<List<TrendDataItemDTO>> getCashFlowTrend(UUID businessId, LocalDate fromDate, LocalDate toDate, String granularity) {
        try {
            if (businessId == null) {
                return ServiceResponse.failureResponse(400, "Business ID is required");
            }
            if (permissionScopeHelper.isAssignedScope("dashboard", "view") && permissionScopeHelper.getCurrentUserId().isPresent()) {
                List<UUID> allowed = bookRepository.findBusinessIdsByAssignedUserId(permissionScopeHelper.getCurrentUserId().get());
                if (!allowed.contains(businessId)) {
                    return ServiceResponse.failureResponse(403, "Access denied: you can only view dashboard for assigned businesses.");
                }
            }
            if (businessRepository.findById(businessId).isEmpty()) {
                return ServiceResponse.failureResponse(404, "Business not found");
            }
            List<TrendDataItemDTO> trendData = buildTrendData(businessId, fromDate, toDate, granularity);
            return ServiceResponse.successResponse(200, "Success", trendData != null ? trendData : List.of());
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

    private List<TrendDataItemDTO> buildTrendData(UUID businessId, LocalDate fromDate, LocalDate toDate, String granularity) {
        String g = (granularity != null && !granularity.isBlank()) ? granularity.toLowerCase() : "month";
        List<Object[]> rows;
        switch (g) {
            case "week" -> rows = transactionRepository.getTrendDataWeekly(businessId, fromDate, toDate);
            case "day" -> rows = transactionRepository.getTrendDataDaily(businessId, fromDate, toDate);
            default -> rows = transactionRepository.getTrendDataMonthly(businessId, fromDate, toDate);
        }
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        List<TrendDataItemDTO> result = new ArrayList<>();
        for (Object[] row : rows) {
            String x = row[0] != null ? row[0].toString().trim() : "";
            BigDecimal y = row[1] != null ? new BigDecimal(row[1].toString()) : BigDecimal.ZERO;
            result.add(TrendDataItemDTO.builder().x(x).y(y).build());
        }
        return result;
    }
}
