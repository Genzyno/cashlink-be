package com.john.ledger.entry.service;

import com.john.ledger.common.util.ServiceResponse;
import com.john.ledger.entry.dto.response.analytics.*;

import java.time.LocalDate;
import java.util.UUID;

public interface IAnalyticsService {

    ServiceResponse<CategoryWiseResponse> getCategoryWise(UUID businessId, UUID bookId, LocalDate fromDate, LocalDate toDate);

    ServiceResponse<MonthWiseResponse> getMonthWise(UUID businessId, UUID bookId, LocalDate fromDate, LocalDate toDate);

    ServiceResponse<BusinessWiseResponse> getBusinessWise(LocalDate fromDate, LocalDate toDate);

    ServiceResponse<TimeSeriesResponse> getTimeSeries(UUID businessId, UUID bookId, LocalDate fromDate, LocalDate toDate, String granularity);
}
