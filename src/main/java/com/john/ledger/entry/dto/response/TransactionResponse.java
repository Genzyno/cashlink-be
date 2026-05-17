package com.john.ledger.entry.dto.response;

import com.john.ledger.common.enums.TransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {

    private java.util.UUID id;
    private java.util.UUID businessId;
    private java.util.UUID bookId;
    private TransactionType transactionType;
    private LocalDate date;
    private LocalTime time;
    /** Combined date+time at millisecond (and sub-ms) precision for accurate retrieval/ordering. */
    private LocalDateTime transactionAt;
    private BigDecimal amount;
    private String remarks;
    private java.util.UUID categoryId;
    private String categoryName;
    private java.util.UUID paymentModeId;
    private String paymentModeName;
    private String bookName;
    private List<TransactionFileResponse> billFiles;
    /** Running/cumulative balance after this transaction (for dashboard table). */
    private BigDecimal runningBalance;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private java.util.UUID createdByUserId;
    private String createdByName;
    private java.util.UUID updatedByUserId;
    private String updatedByName;
}
