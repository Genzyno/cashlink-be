package com.john.ledger.entry.dto.response;

import lombok.Data;

@Data
public class BusinessResponse {

    private java.util.UUID id;
    private String businessName;
    private String businessType;
    private String currency;
    private String financialYear;
}
