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
public class CategoryWiseItemDTO {
    private String categoryId;
    private String categoryName;
    private String transactionType;
    private BigDecimal totalAmount;
    private Long transactionCount;
    private Double percentage;
}
