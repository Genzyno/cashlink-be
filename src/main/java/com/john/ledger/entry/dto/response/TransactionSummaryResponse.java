package com.john.ledger.entry.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionSummaryResponse {

    private BigDecimal totalCashIn;
    private BigDecimal totalCashOut;
    private BigDecimal balance;
    private BigDecimal netBalance;
}
