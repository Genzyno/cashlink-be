package com.john.ledger.entry.dto.response.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeSeriesYearItemDTO {
    private Integer year;
    private BigDecimal totalCashIn;
    private BigDecimal totalCashOut;
    private BigDecimal netBalance;
    private Long transactionCount;
}
