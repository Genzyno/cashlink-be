package com.john.ledger.entry.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummaryDTO {
    private BigDecimal totalCashIn;
    private BigDecimal totalCashOut;
    private BigDecimal netBalance;
    private long totalTransactions;
    private long totalBooks;
    private long totalCategories;
}
