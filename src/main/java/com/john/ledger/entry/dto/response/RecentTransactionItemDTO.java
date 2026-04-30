package com.john.ledger.entry.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecentTransactionItemDTO {
    private UUID id;
    private LocalDate date;
    private LocalTime time;
    private String remarks;
    private BigDecimal amount;
    private String type;  // "CASH_IN" or "CASH_OUT"
    private String categoryName;
    private String bookName;
}
