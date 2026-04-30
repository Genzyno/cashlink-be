package com.john.ledger.auth.dto;

import lombok.Data;

@Data
public class AuthVerifyEmailRequest {
    private String token;
}
