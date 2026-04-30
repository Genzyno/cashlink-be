package com.john.ledger.entry.service.impl;

import com.john.ledger.common.util.ServiceResponse;
import com.john.ledger.entry.dto.response.analytics.*;
import com.john.ledger.entry.repository.BookRepository;
import com.john.ledger.entry.repository.TransactionRepository;
import com.john.ledger.entry.service.IAnalyticsService;
import com.john.ledger.entry.util.PermissionScopeHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AnalyticsService implements IAnalyticsService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private PermissionScopeHelper permissionScopeHelper;

    @Override
    public ServiceResponse<CategoryWiseResponse> getCategoryWise(UUID businessId, UUID bookId, LocalDate fromDate, LocalDate toDate) {
        try {
            validateDateRange(fromDate, toDate);
            applyAssignedScopeBusiness(businessId);
            if (bookId != null) applyAssignedScopeBook(bookId);

            List<Object[]> overviewRows = transactionRepository.getAnalyticsOverview(businessId, bookId, fromDate, toDate);
            AnalyticsOverviewDTO overview = buildOverview(overviewRows, fromDate, toDate, "month");

            List<Object[]> rows = transactionRepository.getAnalyticsCategoryWise(businessId, bookId, fromDate, toDate);
            BigDecimal totalIn = overview.getTotalCashIn();
            BigDecimal totalOut = overview.getTotalCashOut();
            List<CategoryWiseItemDTO> byCategory = new ArrayList<>();
            for (Object[] r : rows) {
                String catId = r[0] != null && !r[0].toString().isEmpty() ? r[0].toString() : null;
                String catName = r[1] != null ? r[1].toString() : "Uncategorized";
                String txnType = r[2] != null ? r[2].toString() : null;
                BigDecimal total = r[3] != null ? new BigDecimal(r[3].toString()) : BigDecimal.ZERO;
                Long count = r[4] != null ? Long.parseLong(r[4].toString()) : 0L;
                double pct = 0;
                if ("CASH_IN".equals(txnType) && totalIn.compareTo(BigDecimal.ZERO) > 0) {
                    pct = total.divide(totalIn, 4, RoundingMode.HALF_UP).doubleValue() * 100;
                } else if ("CASH_OUT".equals(txnType) && totalOut.compareTo(BigDecimal.ZERO) > 0) {
                    pct = total.divide(totalOut, 4, RoundingMode.HALF_UP).doubleValue() * 100;
                }
                byCategory.add(CategoryWiseItemDTO.builder()
                        .categoryId(catId)
                        .categoryName(catName)
                        .transactionType(txnType)
                        .totalAmount(total)
                        .transactionCount(count)
                        .percentage(Math.round(pct * 100.0) / 100.0)
                        .build());
            }
            CategoryWiseResponse data = CategoryWiseResponse.builder().overview(overview).byCategory(byCategory).build();
            return ServiceResponse.successResponse(200, "Success", data);
        } catch (IllegalArgumentException e) {
            return ServiceResponse.failureResponse(400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, "Internal error");
        }
    }

    @Override
    public ServiceResponse<MonthWiseResponse> getMonthWise(UUID businessId, UUID bookId, LocalDate fromDate, LocalDate toDate) {
        try {
            validateDateRange(fromDate, toDate);
            applyAssignedScopeBusiness(businessId);
            if (bookId != null) applyAssignedScopeBook(bookId);

            List<Object[]> overviewRows = transactionRepository.getAnalyticsOverview(businessId, bookId, fromDate, toDate);
            AnalyticsOverviewDTO overview = buildOverview(overviewRows, fromDate, toDate, null);

            List<Object[]> rows = transactionRepository.getAnalyticsMonthWise(businessId, bookId, fromDate, toDate);
            List<MonthWiseItemDTO> byMonth = new ArrayList<>();
            for (Object[] r : rows) {
                int year = r[0] != null ? ((Number) r[0]).intValue() : 0;
                int month = r[1] != null ? ((Number) r[1]).intValue() : 0;
                BigDecimal in = r[2] != null ? new BigDecimal(r[2].toString()) : BigDecimal.ZERO;
                BigDecimal out = r[3] != null ? new BigDecimal(r[3].toString()) : BigDecimal.ZERO;
                Long count = r[4] != null ? Long.parseLong(r[4].toString()) : 0L;
                String period = String.format("%d-%02d", year, month);
                byMonth.add(MonthWiseItemDTO.builder()
                        .period(period)
                        .year(year)
                        .month(month)
                        .totalCashIn(in)
                        .totalCashOut(out)
                        .netBalance(in.subtract(out))
                        .transactionCount(count)
                        .build());
            }
            MonthWiseResponse data = MonthWiseResponse.builder().overview(overview).byMonth(byMonth).build();
            return ServiceResponse.successResponse(200, "Success", data);
        } catch (IllegalArgumentException e) {
            return ServiceResponse.failureResponse(400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, "Internal error");
        }
    }

    @Override
    public ServiceResponse<BusinessWiseResponse> getBusinessWise(LocalDate fromDate, LocalDate toDate) {
        try {
            validateDateRange(fromDate, toDate);

            List<Object[]> overviewRows = transactionRepository.getAnalyticsOverview(null, null, fromDate, toDate);
            AnalyticsOverviewDTO overview = buildOverview(overviewRows, fromDate, toDate, null);

            List<Object[]> rows = transactionRepository.getAnalyticsBusinessWise(fromDate, toDate);
            Optional<UUID> userId = permissionScopeHelper.getCurrentUserId();
            List<BusinessWiseItemDTO> byBusiness = new ArrayList<>();
            List<UUID> allowedBusinessIds = userId.isPresent()
                    ? bookRepository.findBusinessIdsByAssignedUserId(userId.get())
                    : null;
            for (Object[] r : rows) {
                UUID bid = r[0] != null ? UUID.fromString(r[0].toString()) : null;
                if (bid != null && allowedBusinessIds != null && !allowedBusinessIds.isEmpty() && !allowedBusinessIds.contains(bid)) {
                    continue;
                }
                String name = r[1] != null ? r[1].toString() : "";
                BigDecimal in = r[2] != null ? new BigDecimal(r[2].toString()) : BigDecimal.ZERO;
                BigDecimal out = r[3] != null ? new BigDecimal(r[3].toString()) : BigDecimal.ZERO;
                Long count = r[4] != null ? Long.parseLong(r[4].toString()) : 0L;
                Long books = r[5] != null ? Long.parseLong(r[5].toString()) : 0L;
                byBusiness.add(BusinessWiseItemDTO.builder()
                        .businessId(bid != null ? bid.toString() : null)
                        .businessName(name)
                        .totalCashIn(in)
                        .totalCashOut(out)
                        .netBalance(in.subtract(out))
                        .transactionCount(count)
                        .bookCount(books)
                        .build());
            }
            BusinessWiseResponse data = BusinessWiseResponse.builder().overview(overview).byBusiness(byBusiness).build();
            return ServiceResponse.successResponse(200, "Success", data);
        } catch (IllegalArgumentException e) {
            return ServiceResponse.failureResponse(400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, "Internal error");
        }
    }

    @Override
    public ServiceResponse<TimeSeriesResponse> getTimeSeries(UUID businessId, UUID bookId, LocalDate fromDate, LocalDate toDate, String granularity) {
        try {
            validateDateRange(fromDate, toDate);
            applyAssignedScopeBusiness(businessId);
            if (bookId != null) applyAssignedScopeBook(bookId);

            String gran = (granularity != null && !granularity.isBlank()) ? granularity.trim().toLowerCase() : "month";
            if (!gran.equals("day") && !gran.equals("month") && !gran.equals("year")) {
                gran = "month";
            }

            List<Object[]> overviewRows = transactionRepository.getAnalyticsOverview(businessId, bookId, fromDate, toDate);
            AnalyticsOverviewDTO overview = buildOverview(overviewRows, fromDate, toDate, gran);

            List<TimeSeriesDayItemDTO> byDay = null;
            List<MonthWiseItemDTO> byMonth = null;
            List<TimeSeriesYearItemDTO> byYear = null;

            if ("day".equals(gran)) {
                List<Object[]> rows = transactionRepository.getAnalyticsByDay(businessId, bookId, fromDate, toDate);
                byDay = new ArrayList<>();
                for (Object[] r : rows) {
                    String dateStr = r[0] != null ? r[0].toString() : null;
                    BigDecimal in = r[1] != null ? new BigDecimal(r[1].toString()) : BigDecimal.ZERO;
                    BigDecimal out = r[2] != null ? new BigDecimal(r[2].toString()) : BigDecimal.ZERO;
                    Long count = r[3] != null ? Long.parseLong(r[3].toString()) : 0L;
                    byDay.add(TimeSeriesDayItemDTO.builder()
                            .date(dateStr)
                            .totalCashIn(in)
                            .totalCashOut(out)
                            .netBalance(in.subtract(out))
                            .transactionCount(count)
                            .build());
                }
            } else if ("month".equals(gran)) {
                List<Object[]> rows = transactionRepository.getAnalyticsMonthWise(businessId, bookId, fromDate, toDate);
                byMonth = new ArrayList<>();
                for (Object[] r : rows) {
                    int year = r[0] != null ? ((Number) r[0]).intValue() : 0;
                    int month = r[1] != null ? ((Number) r[1]).intValue() : 0;
                    BigDecimal in = r[2] != null ? new BigDecimal(r[2].toString()) : BigDecimal.ZERO;
                    BigDecimal out = r[3] != null ? new BigDecimal(r[3].toString()) : BigDecimal.ZERO;
                    Long count = r[4] != null ? Long.parseLong(r[4].toString()) : 0L;
                    byMonth.add(MonthWiseItemDTO.builder()
                            .period(String.format("%d-%02d", year, month))
                            .year(year)
                            .month(month)
                            .totalCashIn(in)
                            .totalCashOut(out)
                            .netBalance(in.subtract(out))
                            .transactionCount(count)
                            .build());
                }
            } else {
                List<Object[]> rows = transactionRepository.getAnalyticsByYear(businessId, bookId, fromDate, toDate);
                byYear = new ArrayList<>();
                for (Object[] r : rows) {
                    int year = r[0] != null ? ((Number) r[0]).intValue() : 0;
                    BigDecimal in = r[1] != null ? new BigDecimal(r[1].toString()) : BigDecimal.ZERO;
                    BigDecimal out = r[2] != null ? new BigDecimal(r[2].toString()) : BigDecimal.ZERO;
                    Long count = r[3] != null ? Long.parseLong(r[3].toString()) : 0L;
                    byYear.add(TimeSeriesYearItemDTO.builder()
                            .year(year)
                            .totalCashIn(in)
                            .totalCashOut(out)
                            .netBalance(in.subtract(out))
                            .transactionCount(count)
                            .build());
                }
            }

            TimeSeriesResponse data = TimeSeriesResponse.builder()
                    .overview(overview)
                    .byDay(byDay)
                    .byMonth(byMonth)
                    .byYear(byYear)
                    .build();
            return ServiceResponse.successResponse(200, "Success", data);
        } catch (IllegalArgumentException e) {
            return ServiceResponse.failureResponse(400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, "Internal error");
        }
    }

    private void validateDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null) {
            throw new IllegalArgumentException("fromDate and toDate are required");
        }
        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("fromDate must be before or equal to toDate");
        }
    }

    private void applyAssignedScopeBusiness(UUID businessId) {
        if (businessId == null) return;
        if (permissionScopeHelper.isAssignedScope("dashboard", "view") && permissionScopeHelper.getCurrentUserId().isPresent()) {
            List<UUID> allowed = bookRepository.findBusinessIdsByAssignedUserId(permissionScopeHelper.getCurrentUserId().get());
            if (!allowed.contains(businessId)) {
                throw new IllegalArgumentException("Access denied: you can only view analytics for assigned businesses.");
            }
        }
    }

    private void applyAssignedScopeBook(UUID bookId) {
        if (bookId == null) return;
        if (permissionScopeHelper.isAssignedScope("dashboard", "view") && permissionScopeHelper.getCurrentUserId().isPresent()) {
            boolean allowed = bookRepository.existsByIdAndAssignedUserId(bookId, permissionScopeHelper.getCurrentUserId().get());
            if (!allowed) {
                throw new IllegalArgumentException("Access denied: you can only view analytics for assigned books.");
            }
        }
    }

    private AnalyticsOverviewDTO buildOverview(List<Object[]> rows, LocalDate fromDate, LocalDate toDate, String granularity) {
        BigDecimal totalIn = BigDecimal.ZERO;
        BigDecimal totalOut = BigDecimal.ZERO;
        long totalTx = 0;
        if (rows != null && !rows.isEmpty()) {
            Object[] r = rows.get(0);
            totalIn = r[0] != null ? new BigDecimal(r[0].toString()) : BigDecimal.ZERO;
            totalOut = r[1] != null ? new BigDecimal(r[1].toString()) : BigDecimal.ZERO;
            totalTx = r[2] != null ? Long.parseLong(r[2].toString()) : 0L;
        }
        return AnalyticsOverviewDTO.builder()
                .totalCashIn(totalIn)
                .totalCashOut(totalOut)
                .netBalance(totalIn.subtract(totalOut))
                .totalTransactions(totalTx)
                .fromDate(fromDate != null ? fromDate.format(DATE_FMT) : null)
                .toDate(toDate != null ? toDate.format(DATE_FMT) : null)
                .granularity(granularity)
                .build();
    }
}
