package com.john.ledger.entry.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrendDataItemDTO {
    private String x;  // e.g. "Jan", "Feb"
    private BigDecimal y;  // value for chart
}
