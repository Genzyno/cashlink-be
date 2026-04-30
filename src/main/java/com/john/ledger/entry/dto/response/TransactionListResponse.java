package com.john.ledger.entry.dto.response;

import com.john.ledger.common.util.PaginationMeta;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionListResponse {

    private List<TransactionResponse> content;
    private PaginationMeta meta;
    private TransactionSummaryResponse summary;
}
