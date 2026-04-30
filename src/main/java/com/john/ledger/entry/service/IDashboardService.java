package com.john.ledger.entry.service;

import com.john.ledger.common.util.ServiceResponse;
import com.john.ledger.entry.dto.response.DashboardOverviewResponse;
import com.john.ledger.entry.dto.response.TrendDataItemDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface IDashboardService {

    ServiceResponse<DashboardOverviewResponse> getOverview(UUID businessId, LocalDate fromDate, LocalDate toDate);

    ServiceResponse<List<TrendDataItemDTO>> getCashFlowTrend(UUID businessId, LocalDate fromDate, LocalDate toDate, String granularity);
}
