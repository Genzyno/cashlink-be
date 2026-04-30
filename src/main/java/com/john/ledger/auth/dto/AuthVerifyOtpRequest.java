package com.john.ledger.auth.dto;

import lombok.Data;

@Data
public class AuthVerifyOtpRequest {
    private String email;
    private String otp;
    private String channel;
    private String clientId;
    /** "login" = do not create user; if not registered return 403. "register" = create user with Super Admin role if new. Omit = login. */
    private String intent;
}
