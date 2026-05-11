package com.john.ledger.auth.dto;

import lombok.Data;

@Data
public class AuthLoginRequest {
    private String email;
    private String password;
    private String channel;
    private String clientId;
}
