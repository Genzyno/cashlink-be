package com.john.ledger.entry.controller;

import com.john.ledger.common.enums.TransactionType;
import com.john.ledger.common.util.ServiceResponse;
import com.john.ledger.entry.dto.request.TransactionBulkDeleteRequest;
import com.john.ledger.entry.dto.request.TransactionExportRequest;
import com.john.ledger.entry.dto.request.TransactionSaveRequest;
import com.john.ledger.entry.dto.request.TransactionUpdateRequest;
import com.john.ledger.entry.dto.response.ExportExcelResult;
import com.john.ledger.entry.dto.response.TransactionDashboardResponse;
import com.john.ledger.entry.dto.response.TransactionListResponse;
import com.john.ledger.entry.dto.response.TransactionResponse;
import com.john.ledger.entry.dto.response.TransactionSummaryResponse;
import com.john.ledger.entry.repository.TransactionFileRepository;
import com.john.ledger.entry.entity.TransactionFileEntity;
import com.john.ledger.entry.service.ITransactionService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("transaction")
public class TransactionController {

    @Autowired
    private ITransactionService transactionService;

    @Autowired
    private TransactionFileRepository transactionFileRepository;

    // ===================== Save Transaction =====================

    @Operation(summary = "Save Transaction with optional bill files")
    @PostMapping(value = "/save-transaction", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ServiceResponse<TransactionResponse>> saveTransaction(
            @RequestParam("businessId") UUID businessId,
            @RequestParam("bookId") UUID bookId,
            @RequestParam("transactionType") TransactionType transactionType,
            @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam("time") @DateTimeFormat(pattern = "HH:mm") LocalTime time,
            @RequestParam("amount") BigDecimal amount,
            @RequestParam(value = "remarks", required = false) String remarks,
            @RequestParam(value = "categoryId", required = false) UUID categoryId,
            @RequestParam(value = "paymentModeId", required = false) UUID paymentModeId,
            @RequestParam(value = "billFiles", required = false) MultipartFile[] billFiles) {

        ServiceResponse<TransactionResponse> response = null;
        try {
            TransactionSaveRequest request = TransactionSaveRequest.builder()
                    .businessId(businessId)
                    .bookId(bookId)
                    .transactionType(transactionType)
                    .date(date)
                    .time(time)
                    .amount(amount)
                    .remarks(remarks)
                    .categoryId(categoryId)
                    .paymentModeId(paymentModeId)
                    .build();

            response = transactionService.saveTransaction(request, billFiles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
    }

    // ===================== Update Transaction =====================

    @Operation(summary = "Update Transaction with optional bill files")
    @PutMapping(value = "/update-transaction/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ServiceResponse<TransactionResponse>> updateTransaction(
            @PathVariable UUID id,
            @RequestParam("businessId") UUID businessId,
            @RequestParam("bookId") UUID bookId,
            @RequestParam("transactionType") TransactionType transactionType,
            @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam("time") @DateTimeFormat(pattern = "HH:mm") LocalTime time,
            @RequestParam("amount") BigDecimal amount,
            @RequestParam(value = "remarks", required = false) String remarks,
            @RequestParam(value = "categoryId", required = false) UUID categoryId,
            @RequestParam(value = "paymentModeId", required = false) UUID paymentModeId,
            @RequestParam(value = "billFiles", required = false) MultipartFile[] billFiles) {

        ServiceResponse<TransactionResponse> response = null;
        try {
            TransactionUpdateRequest request = TransactionUpdateRequest.builder()
                    .id(id)
                    .businessId(businessId)
                    .bookId(bookId)
                    .transactionType(transactionType)
                    .date(date)
                    .time(time)
                    .amount(amount)
                    .remarks(remarks)
                    .categoryId(categoryId)
                    .paymentModeId(paymentModeId)
                    .build();

            response = transactionService.updateTransaction(id, request, billFiles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
    }

    // ===================== Delete Transaction =====================

    @Operation(summary = "Delete Transaction")
    @DeleteMapping("/delete-transaction/{id}")
    public ResponseEntity<ServiceResponse<TransactionResponse>> deleteTransaction(@PathVariable UUID id) {

        ServiceResponse<TransactionResponse> response = null;
        try {
            response = transactionService.deleteTransaction(id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
    }

    // ===================== Bulk Delete Transactions =====================

    @Operation(summary = "Bulk delete transactions by IDs")
    @PostMapping("/bulk-delete")
    public ResponseEntity<ServiceResponse<Integer>> bulkDeleteTransactions(@RequestBody TransactionBulkDeleteRequest request) {
        if (request.getIds() == null || request.getIds().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ServiceResponse.failureResponse(400, "ids array is required and cannot be empty"));
        }
        List<UUID> ids;
        try {
            ids = request.getIds().stream()
                    .map(UUID::fromString)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ServiceResponse.failureResponse(400, "Invalid UUID format in ids array"));
        }
        ServiceResponse<Integer> response;
        try {
            response = transactionService.bulkDeleteTransactions(ids);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
    }

    // ===================== Get All Transactions =====================

    @Operation(summary = "Get paginated transaction list by book (includes summary with netBalance)")
    @GetMapping("/get-all-transaction/{bookId}")
    public ResponseEntity<ServiceResponse<TransactionListResponse>> getAllTransactions(
            @PathVariable UUID bookId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        ServiceResponse<TransactionListResponse> response = null;
        try {
            response = transactionService.getAllTransactions(bookId, page, size);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
    }

    // ===================== Filter Transactions =====================

    @Operation(summary = "Filter transactions by book with date range, type, category, payment mode, amount range, and search")
    @GetMapping("/filter-transaction/{bookId}")
    public ResponseEntity<ServiceResponse<TransactionListResponse>> filterTransactions(
            @PathVariable UUID bookId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toDate,
            @RequestParam(required = false) String transactionType,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID paymentModeId,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) UUID createdBy,
            @RequestParam(required = false) UUID updatedBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        TransactionType type = null;
        if (transactionType != null && !transactionType.isBlank() && !"all".equalsIgnoreCase(transactionType)) {
            try {
                type = TransactionType.valueOf(transactionType.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                // invalid type treated as "all"
            }
        }

        ServiceResponse<TransactionListResponse> response;
        try {
            response = transactionService.filterTransactions(
                    bookId, fromDate, toDate, type,
                    categoryId, paymentModeId, minAmount, maxAmount, searchTerm,
                    createdBy, updatedBy, page, size);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
    }

    // ===================== Search Transactions =====================

    @Operation(summary = "Search transactions by book (includes summary with netBalance)")
    @GetMapping("/search-transaction/{bookId}")
    public ResponseEntity<ServiceResponse<TransactionListResponse>> searchTransactions(
            @PathVariable UUID bookId,
            @RequestParam(defaultValue = "") String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        ServiceResponse<TransactionListResponse> response = null;
        try {
            response = transactionService.searchTransactions(bookId, searchTerm, page, size);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
    }

    // ===================== Dashboard =====================

    @Operation(summary = "Dashboard: summary (CASH IN, CASH OUT, NET BALANCE) + all transactions with runningBalance per row, newest first")
    @GetMapping("/dashboard/{bookId}")
    public ResponseEntity<ServiceResponse<TransactionDashboardResponse>> getDashboard(@PathVariable UUID bookId) {

        ServiceResponse<TransactionDashboardResponse> response = null;
        try {
            response = transactionService.getDashboard(bookId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
    }

    // ===================== Get Transaction Summary =====================

    @Operation(summary = "Get transaction summary (Cash In / Cash Out / Balance) by book")
    @GetMapping("/get-transaction-summary/{bookId}")
    public ResponseEntity<ServiceResponse<TransactionSummaryResponse>> getTransactionSummary(@PathVariable UUID bookId) {

        ServiceResponse<TransactionSummaryResponse> response = null;
        try {
            response = transactionService.getTransactionSummary(bookId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
    }

    // ===================== Export Excel =====================

    @Operation(summary = "Export filtered transactions to Excel (same filters as list; bookId required)")
    @PostMapping(value = "/export-excel", consumes = MediaType.APPLICATION_JSON_VALUE, produces = { MediaType.APPLICATION_JSON_VALUE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" })
    public ResponseEntity<?> exportExcel(@RequestBody TransactionExportRequest request) {
        ExportExcelResult result = transactionService.exportToExcel(request);
        if (result.isSuccess()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.getFilename() + "\"")
                    .body(result.getFileData());
        }
        return ResponseEntity.status(result.getStatusCode())
                .body(ServiceResponse.failureResponse(result.getStatusCode(), result.getMessage()));
    }

    // ===================== Download Bill File =====================

    @Operation(summary = "Download a bill file by file ID")
    @GetMapping("/file/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable UUID fileId) {
        try {
            Optional<TransactionFileEntity> fileEntityOpt = transactionFileRepository.findById(fileId);
            if (fileEntityOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            TransactionFileEntity fileEntity = fileEntityOpt.get();
            Path filePath = Paths.get(fileEntity.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fileEntity.getFileType() != null ? fileEntity.getFileType() : "application/octet-stream"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileEntity.getFileName() + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ===================== View Bill File (inline preview) =====================

    @Operation(summary = "View/preview a bill file by file ID (opens in browser)")
    @GetMapping("/file/view/{fileId}")
    public ResponseEntity<Resource> viewFile(@PathVariable UUID fileId) {
        try {
            Optional<TransactionFileEntity> fileEntityOpt = transactionFileRepository.findById(fileId);
            if (fileEntityOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            TransactionFileEntity fileEntity = fileEntityOpt.get();
            Path filePath = Paths.get(fileEntity.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fileEntity.getFileType() != null ? fileEntity.getFileType() : "application/octet-stream"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileEntity.getFileName() + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
