package com.john.ledger.entry.service.impl;

import com.john.ledger.common.util.*;
import com.john.ledger.entry.util.PermissionScopeHelper;
import com.john.ledger.entry.dto.request.TransactionExportRequest;
import com.john.ledger.entry.dto.request.TransactionSaveRequest;
import com.john.ledger.entry.dto.request.TransactionUpdateRequest;
import com.john.ledger.common.enums.TransactionType;
import com.john.ledger.entry.dto.response.ExportExcelResult;
import com.john.ledger.entry.dto.response.TransactionDashboardResponse;
import com.john.ledger.entry.dto.response.TransactionListResponse;
import com.john.ledger.entry.dto.response.TransactionResponse;
import com.john.ledger.entry.dto.response.TransactionSummaryResponse;
import com.john.ledger.entry.entity.BookCategoryEntity;
import com.john.ledger.entry.entity.PaymentModeEntity;
import com.john.ledger.entry.entity.TransactionEntity;
import com.john.ledger.entry.entity.UserEntity;
import com.john.ledger.entry.entity.TransactionFileEntity;
import com.john.ledger.entry.mapper.TransactionMapper;
import com.john.ledger.entry.repository.BookCategoryRepository;
import com.john.ledger.entry.repository.BookRepository;
import com.john.ledger.entry.repository.PaymentModeRepository;
import com.john.ledger.entry.repository.TransactionRepository;
import com.john.ledger.entry.repository.UserRepository;
import com.john.ledger.entry.service.ITransactionService;
import com.john.ledger.entry.specification.TransactionFilterSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransactionService implements ITransactionService {

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    BookCategoryRepository bookCategoryRepository;

    @Autowired
    PaymentModeRepository paymentModeRepository;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    PermissionScopeHelper permissionScopeHelper;

    @Autowired
    UserRepository userRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        File uploadPath = new File(uploadDir);
        if (!uploadPath.exists()) {
            uploadPath.mkdirs();
            System.out.println("Created upload directory: " + uploadPath.getAbsolutePath());
        }
        System.out.println("File upload directory: " + uploadPath.getAbsolutePath());
    }

    // ===================== Save Transaction =====================

    @Override
    @Transactional
    public ServiceResponse<TransactionResponse> saveTransaction(TransactionSaveRequest request, MultipartFile[] billFiles) {
        try {
            if (request.getBusinessId() == null) {
                return ServiceResponse.failureResponse(400, "businessId is required. Use the business UUID from the API.");
            }
            if (request.getBookId() == null) {
                return ServiceResponse.failureResponse(400, "bookId is required. Use the book UUID from the API.");
            }
            // Validate amount
            if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return ServiceResponse.failureResponse(400, "Amount must be greater than 0");
            }

            // Convert DTO → Entity
            TransactionEntity entity = TransactionMapper.toSaveEntity(request);
            permissionScopeHelper.getCurrentUserId().ifPresent(entity::setCreatedByUserId);

            // Persist entity first to get the ID
            TransactionEntity savedEntity = transactionRepository.save(entity);

            // Save bill files to local storage
            if (billFiles != null && billFiles.length > 0) {
                saveFilesToDisk(savedEntity, billFiles);
                savedEntity = transactionRepository.save(savedEntity);
            }

            // Convert Entity → Response DTO with names
            TransactionResponse responseDto = toEnrichedResponse(savedEntity);

            return ServiceResponse.successResponse(201, ResponseMessages.CREATED, responseDto);
        } catch (IOException e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, "Failed to upload bill files");
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

    // ===================== Update Transaction =====================

    @Override
    @Transactional
    public ServiceResponse<TransactionResponse> updateTransaction(java.util.UUID id, TransactionUpdateRequest request, MultipartFile[] billFiles) {
        try {
            if (id == null) {
                return ServiceResponse.failureResponse(400, "Transaction id is required. Use the transaction UUID from the API.");
            }
            if (request.getBusinessId() == null) {
                return ServiceResponse.failureResponse(400, "businessId is required. Use the business UUID from the API.");
            }
            if (request.getBookId() == null) {
                return ServiceResponse.failureResponse(400, "bookId is required. Use the book UUID from the API.");
            }
            // Validation
            Optional<TransactionEntity> existingTransaction = transactionRepository.findById(id);
            if (existingTransaction.isEmpty()) {
                return ServiceResponse.failureResponse(404, "Transaction Not Found");
            }

            // Validate amount
            if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return ServiceResponse.failureResponse(400, "Amount must be greater than 0");
            }

            TransactionEntity entity = existingTransaction.get();

            // Update fields
            TransactionMapper.toUpdateEntity(request, entity);
            permissionScopeHelper.getCurrentUserId().ifPresent(entity::setUpdatedByUserId);

            // Save new bill files if provided
            if (billFiles != null && billFiles.length > 0) {
                saveFilesToDisk(entity, billFiles);
            }

            // Persist entity
            TransactionEntity savedEntity = transactionRepository.save(entity);

            // Convert Entity → Response DTO with names
            TransactionResponse responseDto = toEnrichedResponse(savedEntity);

            return ServiceResponse.successResponse(200, ResponseMessages.UPDATED, responseDto);
        } catch (IOException e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, "Failed to upload bill files");
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

    // ===================== Delete Transaction =====================

    @Override
    @Transactional
    public ServiceResponse<TransactionResponse> deleteTransaction(java.util.UUID id) {
        try {
            // Validation
            Optional<TransactionEntity> existingTransaction = transactionRepository.findById(id);
            if (existingTransaction.isEmpty()) {
                return ServiceResponse.failureResponse(404, "Transaction Not Found!");
            }

            TransactionEntity entity = existingTransaction.get();

            // Delete files from disk
            deleteFilesFromDisk(entity);

            // Delete entity (cascade will delete file entities)
            transactionRepository.deleteById(id);

            return ServiceResponse.successResponse(200, ResponseMessages.DELETED, null);
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

    // ===================== Bulk Delete Transactions =====================

    @Override
    @Transactional
    public ServiceResponse<Integer> bulkDeleteTransactions(List<java.util.UUID> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return ServiceResponse.failureResponse(400, "ids array is required and cannot be empty");
            }

            List<TransactionEntity> toDelete = transactionRepository.findAllById(ids);

            for (TransactionEntity entity : toDelete) {
                deleteFilesFromDisk(entity);
            }

            transactionRepository.deleteAllById(ids);
            int deletedCount = toDelete.size();

            return ServiceResponse.successResponse(200,
                    deletedCount == 1 ? "1 transaction deleted successfully"
                            : deletedCount + " transactions deleted successfully",
                    deletedCount);
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

    // ===================== Get All Transactions =====================

    @Override
    public ServiceResponse<TransactionListResponse> getAllTransactions(java.util.UUID bookId, int page, int size) {
        try {
            if (bookId == null) {
                return ServiceResponse.failureResponse(400, "bookId is required. Use the book UUID from the API.");
            }
            ServiceResponse<?> accessDenied = checkBookAccessForAssignedScope(bookId);
            if (accessDenied != null) return ServiceResponse.failureResponse(accessDenied.getStatusCode(), accessDenied.getMessage());
            // Sort: newest first by date, then time, then id (same-minute transactions ordered by id DESC)
            Sort sort = Sort.by(Sort.Order.desc("date"), Sort.Order.desc("time"), Sort.Order.desc("id"));
            PageRequest pageRequest = PageRequest.of(page, size, sort);

            Page<TransactionEntity> transactionPage = transactionRepository.findAllByBookId(bookId, pageRequest);

            if (transactionPage.isEmpty()) {
                TransactionSummaryResponse summary = getSummaryForBook(bookId);
                TransactionListResponse listResponse = TransactionListResponse.builder()
                        .content(Collections.emptyList())
                        .meta(new PaginationMeta(page, size, 0, 0, true, true, 0))
                        .summary(summary)
                        .build();
                return ServiceResponse.successResponse(200, ResponseMessages.NO_RECORD, listResponse);
            }

            // Batch-fetch category, payment mode, and user names for all transactions
            Map<java.util.UUID, String> categoryNames = batchFetchCategoryNames(transactionPage.getContent());
            Map<java.util.UUID, String> paymentModeNames = batchFetchPaymentModeNames(transactionPage.getContent());
            Map<java.util.UUID, String> userNames = batchFetchUserNames(transactionPage.getContent());

            Page<TransactionResponse> dtoPage = transactionPage.map(entity -> {
                TransactionResponse response = TransactionMapper.toResponse(entity);
                if (entity.getCategoryId() != null) {
                    response.setCategoryName(categoryNames.get(entity.getCategoryId()));
                }
                if (entity.getPaymentModeId() != null) {
                    response.setPaymentModeName(paymentModeNames.get(entity.getPaymentModeId()));
                }
                if (entity.getCreatedByUserId() != null) {
                    response.setCreatedByName(userNames.get(entity.getCreatedByUserId()));
                }
                if (entity.getUpdatedByUserId() != null) {
                    response.setUpdatedByName(userNames.get(entity.getUpdatedByUserId()));
                }
                return response;
            });

            PaginatedResponse<TransactionResponse> paginatedResponse = PaginationUtil.createPaginatedResponse(dtoPage);
            TransactionSummaryResponse summary = getSummaryForBook(bookId);

            TransactionListResponse listResponse = TransactionListResponse.builder()
                    .content(paginatedResponse.getContent())
                    .meta(paginatedResponse.getMeta())
                    .summary(summary)
                    .build();

            return ServiceResponse.successResponse(200, "Transaction list fetched successfully", listResponse);

        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

    // ===================== Search Transactions =====================

    @Override
    public ServiceResponse<TransactionListResponse> searchTransactions(java.util.UUID bookId, String searchTerm, int page, int size) {
        try {
            if (bookId == null) {
                return ServiceResponse.failureResponse(400, "bookId is required. Use the book UUID from the API.");
            }
            ServiceResponse<?> accessDenied = checkBookAccessForAssignedScope(bookId);
            if (accessDenied != null) return ServiceResponse.failureResponse(accessDenied.getStatusCode(), accessDenied.getMessage());
            // Sort: newest first by date, then time, then id (same-minute transactions ordered by id DESC)
            Sort sort = Sort.by(Sort.Order.desc("date"), Sort.Order.desc("time"), Sort.Order.desc("id"));
            PageRequest pageRequest = PageRequest.of(page, size, sort);

            Page<TransactionEntity> transactionPage;

            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                transactionPage = transactionRepository.findAllByBookId(bookId, pageRequest);
            } else {
                transactionPage = transactionRepository.searchTransactions(bookId, searchTerm.trim(), pageRequest);
            }

            if (transactionPage.isEmpty()) {
                TransactionSummaryResponse summary = getSummaryForBook(bookId);
                TransactionListResponse listResponse = TransactionListResponse.builder()
                        .content(Collections.emptyList())
                        .meta(new PaginationMeta(page, size, 0, 0, true, true, 0))
                        .summary(summary)
                        .build();
                return ServiceResponse.successResponse(200, ResponseMessages.NO_RECORD, listResponse);
            }

            // Batch-fetch category, payment mode, and user names for all transactions
            Map<java.util.UUID, String> categoryNames = batchFetchCategoryNames(transactionPage.getContent());
            Map<java.util.UUID, String> paymentModeNames = batchFetchPaymentModeNames(transactionPage.getContent());
            Map<java.util.UUID, String> userNames = batchFetchUserNames(transactionPage.getContent());

            Page<TransactionResponse> dtoPage = transactionPage.map(entity -> {
                TransactionResponse response = TransactionMapper.toResponse(entity);
                if (entity.getCategoryId() != null) {
                    response.setCategoryName(categoryNames.get(entity.getCategoryId()));
                }
                if (entity.getPaymentModeId() != null) {
                    response.setPaymentModeName(paymentModeNames.get(entity.getPaymentModeId()));
                }
                if (entity.getCreatedByUserId() != null) {
                    response.setCreatedByName(userNames.get(entity.getCreatedByUserId()));
                }
                if (entity.getUpdatedByUserId() != null) {
                    response.setUpdatedByName(userNames.get(entity.getUpdatedByUserId()));
                }
                return response;
            });

            PaginatedResponse<TransactionResponse> paginatedResponse = PaginationUtil.createPaginatedResponse(dtoPage);
            TransactionSummaryResponse summary = getSummaryForBook(bookId);

            TransactionListResponse listResponse = TransactionListResponse.builder()
                    .content(paginatedResponse.getContent())
                    .meta(paginatedResponse.getMeta())
                    .summary(summary)
                    .build();

            return ServiceResponse.successResponse(200, "Transaction list fetched successfully", listResponse);

        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

    // ===================== Filter Transactions =====================

    @Override
    public ServiceResponse<TransactionListResponse> filterTransactions(
            java.util.UUID bookId,
            java.time.LocalDate fromDate,
            java.time.LocalDate toDate,
            TransactionType transactionType,
            java.util.UUID categoryId,
            java.util.UUID paymentModeId,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            String searchTerm,
            java.util.UUID createdByUserId,
            java.util.UUID updatedByUserId,
            int page,
            int size) {
        try {
            if (bookId == null) {
                return ServiceResponse.failureResponse(400, "bookId is required.");
            }
            ServiceResponse<?> accessDenied = checkBookAccessForAssignedScope(bookId);
            if (accessDenied != null) return ServiceResponse.failureResponse(accessDenied.getStatusCode(), accessDenied.getMessage());
            Sort sort = Sort.by(Sort.Order.desc("date"), Sort.Order.desc("time"), Sort.Order.desc("id"));
            PageRequest pageRequest = PageRequest.of(page, size, sort);

            Specification<TransactionEntity> spec = TransactionFilterSpecification.filterBy(
                    bookId, fromDate, toDate, transactionType,
                    categoryId, paymentModeId, minAmount, maxAmount, searchTerm,
                    createdByUserId, updatedByUserId);

            Page<TransactionEntity> transactionPage = transactionRepository.findAll(spec, pageRequest);

            if (transactionPage.isEmpty()) {
                TransactionSummaryResponse summary = getSummaryForBook(bookId);
                TransactionListResponse listResponse = TransactionListResponse.builder()
                        .content(Collections.emptyList())
                        .meta(new PaginationMeta(page, size, 0, 0, true, true, 0))
                        .summary(summary)
                        .build();
                return ServiceResponse.successResponse(200, ResponseMessages.NO_RECORD, listResponse);
            }

            Map<java.util.UUID, String> categoryNames = batchFetchCategoryNames(transactionPage.getContent());
            Map<java.util.UUID, String> paymentModeNames = batchFetchPaymentModeNames(transactionPage.getContent());
            Map<java.util.UUID, String> userNames = batchFetchUserNames(transactionPage.getContent());

            Page<TransactionResponse> dtoPage = transactionPage.map(entity -> {
                TransactionResponse response = TransactionMapper.toResponse(entity);
                if (entity.getCategoryId() != null) {
                    response.setCategoryName(categoryNames.get(entity.getCategoryId()));
                }
                if (entity.getPaymentModeId() != null) {
                    response.setPaymentModeName(paymentModeNames.get(entity.getPaymentModeId()));
                }
                if (entity.getCreatedByUserId() != null) {
                    response.setCreatedByName(userNames.get(entity.getCreatedByUserId()));
                }
                if (entity.getUpdatedByUserId() != null) {
                    response.setUpdatedByName(userNames.get(entity.getUpdatedByUserId()));
                }
                return response;
            });

            PaginatedResponse<TransactionResponse> paginatedResponse = PaginationUtil.createPaginatedResponse(dtoPage);
            TransactionSummaryResponse summary = getSummaryForBook(bookId);

            TransactionListResponse listResponse = TransactionListResponse.builder()
                    .content(paginatedResponse.getContent())
                    .meta(paginatedResponse.getMeta())
                    .summary(summary)
                    .build();

            return ServiceResponse.successResponse(200, "Filtered transactions fetched successfully", listResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

    private static final int EXPORT_MAX_ROWS = 10_000;

    @Override
    public ExportExcelResult exportToExcel(TransactionExportRequest request) {
        try {
            if (request == null || request.getBookId() == null) {
                return ExportExcelResult.failure(400, "bookId is required.");
            }
            java.util.UUID bookId = request.getBookId();
            ServiceResponse<?> accessDenied = checkBookAccessForAssignedScope(bookId);
            if (accessDenied != null) {
                return ExportExcelResult.failure(accessDenied.getStatusCode(), accessDenied.getMessage());
            }

            List<TransactionType> transactionTypesList = parseTransactionTypes(request.getTransactionTypes());

            Specification<TransactionEntity> spec = TransactionFilterSpecification.filterByExport(
                    bookId,
                    request.getFromDate(),
                    request.getToDate(),
                    transactionTypesList,
                    emptyToNull(request.getCategoryIds()),
                    emptyToNull(request.getPaymentModeIds()),
                    request.getMinAmount(),
                    request.getMaxAmount(),
                    emptyToNull(request.getCreatedByIds()),
                    emptyToNull(request.getUpdatedByIds()));

            Sort sort = Sort.by(Sort.Order.desc("date"), Sort.Order.desc("time"), Sort.Order.desc("id"));
            PageRequest pageRequest = PageRequest.of(0, EXPORT_MAX_ROWS, sort);
            List<TransactionEntity> list = transactionRepository.findAll(spec, pageRequest).getContent();

            Map<java.util.UUID, String> categoryNames = batchFetchCategoryNames(list);
            Map<java.util.UUID, String> paymentModeNames = batchFetchPaymentModeNames(list);
            Map<java.util.UUID, String> userNames = batchFetchUserNames(list);

            byte[] excelBytes = buildExcelWorkbook(list, categoryNames, paymentModeNames, userNames);
            return ExportExcelResult.ok(excelBytes, "transactions_export.xlsx");
        } catch (Exception e) {
            e.printStackTrace();
            return ExportExcelResult.failure(500, e.getMessage() != null ? e.getMessage() : ResponseMessages.INTERNAL_ERROR);
        }
    }

    private static List<TransactionType> parseTransactionTypes(List<String> transactionTypes) {
        if (transactionTypes == null || transactionTypes.isEmpty()) return null;
        List<TransactionType> result = new ArrayList<>();
        for (String s : transactionTypes) {
            if (s == null || s.isBlank()) continue;
            try {
                result.add(TransactionType.valueOf(s.trim().toUpperCase()));
            } catch (IllegalArgumentException ignored) { /* skip invalid */ }
        }
        return result.isEmpty() ? null : result;
    }

    private static <T> List<T> emptyToNull(List<T> list) {
        return (list == null || list.isEmpty()) ? null : list;
    }

    private byte[] buildExcelWorkbook(List<TransactionEntity> transactions,
                                      Map<java.util.UUID, String> categoryNames,
                                      Map<java.util.UUID, String> paymentModeNames,
                                      Map<java.util.UUID, String> userNames) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Transactions");
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            String[] headers = { "Date", "Time", "Type", "Amount", "Category", "Payment Mode", "Remarks", "Created By", "Updated By" };
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (TransactionEntity e : transactions) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(e.getDate() != null ? e.getDate().toString() : "");
                row.createCell(1).setCellValue(e.getTime() != null ? e.getTime().toString() : "");
                row.createCell(2).setCellValue(e.getTransactionType() != null ? e.getTransactionType().name() : "");
                row.createCell(3).setCellValue(e.getAmount() != null ? e.getAmount().doubleValue() : 0);
                row.createCell(4).setCellValue(e.getCategoryId() != null ? categoryNames.getOrDefault(e.getCategoryId(), "") : "");
                row.createCell(5).setCellValue(e.getPaymentModeId() != null ? paymentModeNames.getOrDefault(e.getPaymentModeId(), "") : "");
                row.createCell(6).setCellValue(e.getRemarks() != null ? e.getRemarks() : "");
                row.createCell(7).setCellValue(e.getCreatedByUserId() != null ? userNames.getOrDefault(e.getCreatedByUserId(), "") : "");
                row.createCell(8).setCellValue(e.getUpdatedByUserId() != null ? userNames.getOrDefault(e.getUpdatedByUserId(), "") : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    // ===================== Get Transaction Summary =====================

    @Override
    public ServiceResponse<TransactionSummaryResponse> getTransactionSummary(java.util.UUID bookId) {
        try {
            if (bookId == null) {
                return ServiceResponse.failureResponse(400, "bookId is required. Use the book UUID from the API.");
            }
            ServiceResponse<?> accessDenied = checkBookAccessForAssignedScope(bookId);
            if (accessDenied != null) return ServiceResponse.failureResponse(accessDenied.getStatusCode(), accessDenied.getMessage());
            List<Object[]> result = transactionRepository.getTransactionSummary(bookId);

            BigDecimal totalCashIn = BigDecimal.ZERO;
            BigDecimal totalCashOut = BigDecimal.ZERO;

            if (result != null && !result.isEmpty()) {
                Object[] row = result.get(0);
                totalCashIn = new BigDecimal(row[0].toString());
                totalCashOut = new BigDecimal(row[1].toString());
            }

            BigDecimal balance = totalCashIn.subtract(totalCashOut);
            BigDecimal netBalance = balance;

            TransactionSummaryResponse summary = TransactionSummaryResponse.builder()
                    .totalCashIn(totalCashIn)
                    .totalCashOut(totalCashOut)
                    .balance(balance)
                    .netBalance(netBalance)
                    .build();

            return ServiceResponse.successResponse(200, "Transaction summary fetched successfully", summary);

        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

    // ===================== Dashboard =====================

    @Override
    public ServiceResponse<TransactionDashboardResponse> getDashboard(java.util.UUID bookId) {
        try {
            if (bookId == null) {
                return ServiceResponse.failureResponse(400, "bookId is required. Use the book UUID from the API.");
            }
            ServiceResponse<?> accessDenied = checkBookAccessForAssignedScope(bookId);
            if (accessDenied != null) return ServiceResponse.failureResponse(accessDenied.getStatusCode(), accessDenied.getMessage());
            List<TransactionEntity> chronological = transactionRepository.findAllByBookIdOrderByDateAscTimeAscIdAsc(bookId);

            BigDecimal runningBalance = BigDecimal.ZERO;
            List<TransactionResponse> withRunning = new ArrayList<>();

            for (TransactionEntity entity : chronological) {
                if (entity.getTransactionType() == TransactionType.CASH_IN) {
                    runningBalance = runningBalance.add(entity.getAmount());
                } else {
                    runningBalance = runningBalance.subtract(entity.getAmount());
                }
                TransactionResponse response = TransactionMapper.toResponse(entity);
                response.setRunningBalance(runningBalance);
                withRunning.add(response);
            }

            Map<java.util.UUID, String> categoryNames = batchFetchCategoryNames(chronological);
            Map<java.util.UUID, String> paymentModeNames = batchFetchPaymentModeNames(chronological);
            Map<java.util.UUID, String> userNames = batchFetchUserNames(chronological);
            for (TransactionResponse r : withRunning) {
                if (r.getCategoryId() != null) r.setCategoryName(categoryNames.get(r.getCategoryId()));
                if (r.getPaymentModeId() != null) r.setPaymentModeName(paymentModeNames.get(r.getPaymentModeId()));
                if (r.getCreatedByUserId() != null) r.setCreatedByName(userNames.get(r.getCreatedByUserId()));
                if (r.getUpdatedByUserId() != null) r.setUpdatedByName(userNames.get(r.getUpdatedByUserId()));
            }

            TransactionSummaryResponse summary = getSummaryForBook(bookId);
            TransactionDashboardResponse dashboard = TransactionDashboardResponse.builder()
                    .summary(summary)
                    .transactions(withRunning)
                    .build();

            return ServiceResponse.successResponse(200, "Dashboard data fetched successfully", dashboard);
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

    /** When scope is "assigned", verifies the book is assigned to the current user. Returns failure response or null if allowed. */
    private ServiceResponse<?> checkBookAccessForAssignedScope(java.util.UUID bookId) {
        if (!permissionScopeHelper.isAssignedScope("transactions", "view")) return null;
        Optional<java.util.UUID> userId = permissionScopeHelper.getCurrentUserId();
        if (userId.isEmpty()) return null;
        if (!bookRepository.existsByIdAndAssignedUserId(bookId, userId.get())) {
            return ServiceResponse.failureResponse(403, "Access denied: you can only view transactions for assigned books.");
        }
        return null;
    }

    /**
     * Builds summary (totalCashIn, totalCashOut, balance, netBalance) for a book.
     * Used by get-transaction-summary and by get-all/search so list response includes net balance.
     */
    private TransactionSummaryResponse getSummaryForBook(java.util.UUID bookId) {
        List<Object[]> result = transactionRepository.getTransactionSummary(bookId);
        BigDecimal totalCashIn = BigDecimal.ZERO;
        BigDecimal totalCashOut = BigDecimal.ZERO;
        if (result != null && !result.isEmpty()) {
            Object[] row = result.get(0);
            totalCashIn = new BigDecimal(row[0].toString());
            totalCashOut = new BigDecimal(row[1].toString());
        }
        BigDecimal balance = totalCashIn.subtract(totalCashOut);
        return TransactionSummaryResponse.builder()
                .totalCashIn(totalCashIn)
                .totalCashOut(totalCashOut)
                .balance(balance)
                .netBalance(balance)
                .build();
    }

    // ===================== Enrichment Helpers =====================

    /**
     * Maps entity to response and enriches with category name and payment mode name.
     * Used for single-record responses (save, update).
     */
    private TransactionResponse toEnrichedResponse(TransactionEntity entity) {
        TransactionResponse response = TransactionMapper.toResponse(entity);

        if (entity.getCategoryId() != null) {
            bookCategoryRepository.findById(entity.getCategoryId())
                    .ifPresent(cat -> response.setCategoryName(cat.getCategoryName()));
        }
        if (entity.getPaymentModeId() != null) {
            paymentModeRepository.findById(entity.getPaymentModeId())
                    .ifPresent(pm -> response.setPaymentModeName(pm.getPaymentModeName()));
        }
        if (entity.getCreatedByUserId() != null) {
            userRepository.findById(entity.getCreatedByUserId())
                    .ifPresent(u -> response.setCreatedByName(u.getUserName()));
        }
        if (entity.getUpdatedByUserId() != null) {
            userRepository.findById(entity.getUpdatedByUserId())
                    .ifPresent(u -> response.setUpdatedByName(u.getUserName()));
        }

        return response;
    }

    /**
     * Batch-fetch all category names for a list of transactions (avoids N+1 queries).
     */
    private Map<java.util.UUID, String> batchFetchCategoryNames(List<TransactionEntity> transactions) {
        Set<java.util.UUID> categoryIds = transactions.stream()
                .map(TransactionEntity::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (categoryIds.isEmpty()) return Collections.emptyMap();

        return bookCategoryRepository.findAllById(categoryIds).stream()
                .collect(Collectors.toMap(BookCategoryEntity::getId, BookCategoryEntity::getCategoryName));
    }

    /**
     * Batch-fetch all payment mode names for a list of transactions (avoids N+1 queries).
     */
    private Map<java.util.UUID, String> batchFetchPaymentModeNames(List<TransactionEntity> transactions) {
        Set<java.util.UUID> paymentModeIds = transactions.stream()
                .map(TransactionEntity::getPaymentModeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (paymentModeIds.isEmpty()) return Collections.emptyMap();

        return paymentModeRepository.findAllById(paymentModeIds).stream()
                .collect(Collectors.toMap(PaymentModeEntity::getId, PaymentModeEntity::getPaymentModeName));
    }

    /**
     * Batch-fetch user names for createdBy/updatedBy user IDs (avoids N+1 queries).
     */
    private Map<java.util.UUID, String> batchFetchUserNames(List<TransactionEntity> transactions) {
        Set<java.util.UUID> userIds = new java.util.HashSet<>();
        for (TransactionEntity t : transactions) {
            if (t.getCreatedByUserId() != null) userIds.add(t.getCreatedByUserId());
            if (t.getUpdatedByUserId() != null) userIds.add(t.getUpdatedByUserId());
        }
        if (userIds.isEmpty()) return Collections.emptyMap();
        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, UserEntity::getUserName));
    }

    // ===================== File Storage Helpers =====================

    /**
     * Saves uploaded files to local disk organized by businessId/bookId/transactionId.
     * Path: D:/MY_LEDGER_FILES/BUSINESS/{businessId}/{bookId}/txn_{transactionId}/{uuid}_{originalFileName}
     */
    private void saveFilesToDisk(TransactionEntity transaction, MultipartFile[] files) throws IOException {

        Path dirPath = Paths.get(uploadDir,
                String.valueOf(transaction.getBusinessId()),
                String.valueOf(transaction.getBookId()),
                "txn_" + transaction.getId());
        Files.createDirectories(dirPath);

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            String originalFileName = file.getOriginalFilename();
            String uniqueFileName = UUID.randomUUID().toString().substring(0, 8) + "_" + originalFileName;

            Path filePath = dirPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Create file entity
            TransactionFileEntity fileEntity = TransactionFileEntity.builder()
                    .transaction(transaction)
                    .fileName(originalFileName)
                    .filePath(filePath.toString())
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .build();

            transaction.getBillFiles().add(fileEntity);
        }
    }

    /**
     * Deletes all files from disk for a given transaction.
     */
    private void deleteFilesFromDisk(TransactionEntity transaction) {
        try {
            if (transaction.getBillFiles() != null) {
                for (TransactionFileEntity fileEntity : transaction.getBillFiles()) {
                    Path filePath = Paths.get(fileEntity.getFilePath());
                    Files.deleteIfExists(filePath);
                }
            }

            // Try to delete the transaction directory if empty
            Path txnDir = Paths.get(uploadDir,
                    String.valueOf(transaction.getBusinessId()),
                    String.valueOf(transaction.getBookId()),
                    "txn_" + transaction.getId());
            if (Files.exists(txnDir)) {
                Files.deleteIfExists(txnDir);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
