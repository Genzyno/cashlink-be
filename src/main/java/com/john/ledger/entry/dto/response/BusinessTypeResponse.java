package com.john.ledger.entry.dto.response;

import lombok.Data;

@Data
public class BusinessTypeResponse {

    private java.util.UUID id;
    private String businessType;
    private java.util.UUID adminId;

}
