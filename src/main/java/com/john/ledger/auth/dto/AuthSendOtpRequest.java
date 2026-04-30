package com.john.ledger.auth.dto;

import lombok.Data;

@Data
public class AuthSendOtpRequest {
    private String email;
    private String channel;
    private String clientId;
}
