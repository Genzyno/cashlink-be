package com.john.ledger.auth.dto;

import lombok.Data;

@Data
public class AuthRefreshRequest {
    private String refreshToken;
}
