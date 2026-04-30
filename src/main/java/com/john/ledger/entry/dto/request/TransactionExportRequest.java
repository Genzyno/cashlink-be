package com.john.ledger.entry.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Request body for POST /transaction/export-excel.
 * Multiselect filter fields (arrays); only bookId is required. Empty or omitted arrays = no restriction.
 */
@Getter
@Setter
public class TransactionExportRequest {

    private UUID bookId;
    private LocalDate fromDate;
    private LocalDate toDate;
    /** CASH_IN, CASH_OUT. Empty/omit = both. */
    private List<String> transactionTypes;
    private List<UUID> categoryIds;
    private List<UUID> paymentModeIds;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    /** Filter by creator user ids. Empty/omit = all. */
    private List<UUID> createdByIds;
    /** Filter by last updater user ids. Empty/omit = all. */
    private List<UUID> updatedByIds;
}
