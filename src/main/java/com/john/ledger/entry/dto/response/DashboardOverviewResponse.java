package com.john.ledger.entry.dto.response;

import lombok.*;

import java.util.List;

/** Dashboard overview API response: summary, overview, recent transactions, trend data. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardOverviewResponse {
    private DashboardSummaryDTO summary;
    private DashboardOverviewDTO overview;
    private List<RecentTransactionItemDTO> recentTransactions;
    private List<TrendDataItemDTO> trendData;
}
