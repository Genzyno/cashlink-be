package com.john.ledger.entry.service;

import com.john.ledger.common.util.ServiceResponse;
import com.john.ledger.entry.dto.response.analytics.*;

import java.time.LocalDate;

public interface IAnalyticsService {

    ServiceResponse<CategoryWiseResponse> getCategoryWise(java.util.UUID businessId, java.util.UUID bookId, LocalDate fromDate, LocalDate toDate);

    ServiceResponse<MonthWiseResponse> getMonthWise(java.util.UUID businessId, java.util.UUID bookId, LocalDate fromDate, LocalDate toDate);

    ServiceResponse<BusinessWiseResponse> getBusinessWise(LocalDate fromDate, LocalDate toDate);

    ServiceResponse<TimeSeriesResponse> getTimeSeries(java.util.UUID businessId, java.util.UUID bookId, LocalDate fromDate, LocalDate toDate, String granularity);
}

