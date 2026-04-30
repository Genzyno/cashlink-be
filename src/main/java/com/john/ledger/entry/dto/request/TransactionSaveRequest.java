package com.john.ledger.entry.dto.request;

import com.john.ledger.common.enums.TransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionSaveRequest {

    private java.util.UUID businessId;
    private java.util.UUID bookId;
    private TransactionType transactionType;
    private LocalDate date;
    private LocalTime time;
    private BigDecimal amount;
    private String remarks;
    private java.util.UUID categoryId;
    private java.util.UUID paymentModeId;
}
