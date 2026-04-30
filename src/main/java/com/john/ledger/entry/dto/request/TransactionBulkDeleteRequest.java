package com.john.ledger.entry.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class TransactionBulkDeleteRequest {
    private List<String> ids;
}
