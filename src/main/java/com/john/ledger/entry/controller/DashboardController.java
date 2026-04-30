package com.john.ledger.entry.controller;

import com.john.ledger.common.util.ServiceResponse;
import com.john.ledger.entry.dto.response.DashboardOverviewResponse;
import com.john.ledger.entry.dto.response.TrendDataItemDTO;
import com.john.ledger.entry.service.IDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("dashboard")
public class DashboardController {

    @Autowired
    private IDashboardService dashboardService;

    @Operation(summary = "Dashboard overview: summary, overview, recent transactions, trend data. Optional fromDate/toDate (YYYY-MM-DD).")
    @GetMapping("/overview")
    public ResponseEntity<ServiceResponse<DashboardOverviewResponse>> getOverview(
            @RequestParam(value = "businessId", required = false) Optional<UUID> businessId,
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> toDate) {
        if (businessId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ServiceResponse.failureResponse(400, "Business ID is required"));
        }
        LocalDate to = toDate.orElse(LocalDate.now());
        LocalDate from = fromDate.orElse(to.withDayOfMonth(1));
        if (from.isAfter(to)) {
            from = to.withDayOfMonth(1);
        }
        ServiceResponse<DashboardOverviewResponse> response = dashboardService.getOverview(businessId.get(), from, to);
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
    }

    @Operation(summary = "Cash flow trend: bar chart data. Optional fromDate/toDate, granularity=month|week|day.")
    @GetMapping("/cash-flow-trend")
    public ResponseEntity<ServiceResponse<List<TrendDataItemDTO>>> getCashFlowTrend(
            @RequestParam(value = "businessId", required = false) Optional<UUID> businessId,
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> toDate,
            @RequestParam(value = "granularity", required = false, defaultValue = "month") String granularity) {
        if (businessId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ServiceResponse.failureResponse(400, "Business ID is required"));
        }
        LocalDate to = toDate.orElse(LocalDate.now());
        LocalDate from = fromDate.orElse(to.withDayOfMonth(1));
        if (from.isAfter(to)) {
            from = to.withDayOfMonth(1);
        }
        ServiceResponse<List<TrendDataItemDTO>> response = dashboardService.getCashFlowTrend(
                businessId.get(), from, to, granularity);
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
    }
}
