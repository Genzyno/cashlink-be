package com.john.ledger.auth.dto;

import lombok.Data;

@Data
public class AuthSendVerificationEmailRequest {
    private String email;
}
