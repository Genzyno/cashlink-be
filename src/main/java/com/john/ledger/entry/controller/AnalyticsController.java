package com.john.ledger.entry.controller;

import com.john.ledger.common.util.ServiceResponse;
import com.john.ledger.entry.dto.response.analytics.*;
import com.john.ledger.entry.service.IAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("analytics")
public class AnalyticsController {

    //testing for commit cicd pipelies
//    Testing for pipelines
    @Autowired
    private IAnalyticsService analyticsService;

    @Operation(summary = "Category-wise analytics: totals by category and transaction type")
    @GetMapping("/category-wise")
    public ResponseEntity<ServiceResponse<CategoryWiseResponse>> getCategoryWise(
            @RequestParam(required = false) UUID businessId,
            @RequestParam(required = false) UUID bookId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        ServiceResponse<CategoryWiseResponse> response = analyticsService.getCategoryWise(businessId, bookId, fromDate, toDate);
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
    }

    @Operation(summary = "Month-wise analytics: totals per month")
    @GetMapping("/month-wise")
    public ResponseEntity<ServiceResponse<MonthWiseResponse>> getMonthWise(
            @RequestParam(required = false) UUID businessId,
            @RequestParam(required = false) UUID bookId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        ServiceResponse<MonthWiseResponse> response = analyticsService.getMonthWise(businessId, bookId, fromDate, toDate);
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
    }

    @Operation(summary = "Business-wise analytics: totals per business")
    @GetMapping("/business-wise")
    public ResponseEntity<ServiceResponse<BusinessWiseResponse>> getBusinessWise(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        ServiceResponse<BusinessWiseResponse> response = analyticsService.getBusinessWise(fromDate, toDate);
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
    }

    @Operation(summary = "Time-series analytics: by day, month, or year (granularity=day|month|year)")
    @GetMapping("/time-series")
    public ResponseEntity<ServiceResponse<TimeSeriesResponse>> getTimeSeries(
            @RequestParam(required = false) UUID businessId,
            @RequestParam(required = false) UUID bookId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false, defaultValue = "month") String granularity) {
        ServiceResponse<TimeSeriesResponse> response = analyticsService.getTimeSeries(businessId, bookId, fromDate, toDate, granularity);
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
    }
}
