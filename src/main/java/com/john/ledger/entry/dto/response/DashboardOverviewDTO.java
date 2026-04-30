package com.john.ledger.entry.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardOverviewDTO {
    private String businessName;
    private long bookCount;
    private long categoryCount;
}
