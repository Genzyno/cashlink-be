package com.john.ledger.entry.dto.response.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessWiseResponse {
    private AnalyticsOverviewDTO overview;
    private List<BusinessWiseItemDTO> byBusiness;
}
