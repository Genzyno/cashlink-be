package com.john.ledger.entry.dto.response;

import lombok.*;

import java.util.List;

/**
 * Dashboard-specific response: summary (CASH IN, CASH OUT, NET BALANCE) and
 * all transactions in descending order (newest first) with runningBalance per row.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDashboardResponse {

    private TransactionSummaryResponse summary;
    private List<TransactionResponse> transactions;
}
