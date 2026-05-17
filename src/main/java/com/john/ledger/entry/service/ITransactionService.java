package com.john.ledger.entry.service;

import com.john.ledger.common.util.ServiceResponse;
import com.john.ledger.common.util.PaginatedResponse;

import com.john.ledger.common.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import com.john.ledger.entry.dto.request.TransactionExportRequest;
import com.john.ledger.entry.dto.request.TransactionSaveRequest;
import com.john.ledger.entry.dto.request.TransactionUpdateRequest;
import com.john.ledger.entry.dto.response.ExportExcelResult;
import com.john.ledger.entry.dto.response.TransactionDashboardResponse;
import com.john.ledger.entry.dto.response.TransactionListResponse;
import com.john.ledger.entry.dto.response.TransactionResponse;
import com.john.ledger.entry.dto.response.TransactionSummaryResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ITransactionService {

    ServiceResponse<TransactionResponse> saveTransaction(TransactionSaveRequest request, MultipartFile[] billFiles);

    ServiceResponse<TransactionResponse> updateTransaction(UUID id, TransactionUpdateRequest request, MultipartFile[] billFiles);

    ServiceResponse<TransactionResponse> deleteTransaction(UUID id);

    ServiceResponse<Integer> bulkDeleteTransactions(List<UUID> ids);

    ServiceResponse<TransactionListResponse> getAllTransactions(UUID bookId, int page, int size);

    ServiceResponse<TransactionListResponse> searchTransactions(UUID bookId, String searchTerm, int page, int size);

    ServiceResponse<TransactionListResponse> filterTransactions(UUID bookId,
            LocalDate fromDate, LocalDate toDate, TransactionType transactionType,
            UUID categoryId, UUID paymentModeId, BigDecimal minAmount, BigDecimal maxAmount,
            String searchTerm, UUID createdByUserId, UUID updatedByUserId, int page, int size);

    ServiceResponse<TransactionSummaryResponse> getTransactionSummary(UUID bookId);

    /** Dashboard: summary + all transactions (newest first) with runningBalance per row. */
    ServiceResponse<TransactionDashboardResponse> getDashboard(UUID bookId);

    /** Export filtered transactions to Excel. Returns file bytes and filename on success, or statusCode + message on failure. */
    ExportExcelResult exportToExcel(TransactionExportRequest request);

    ServiceResponse<PaginatedResponse<TransactionResponse>> getUserHistory(UUID userId, UUID businessId, int page, int size);
}
