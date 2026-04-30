package com.john.ledger.entry.dto.request;

import lombok.Data;

@Data
public class BusinessUpdateRequest {

    private String businessName;
    private java.util.UUID businessTypeId;
    private String currency;
    private String financialYear;
}
