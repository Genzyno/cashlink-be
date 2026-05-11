package com.john.ledger.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResetPasswordRequest {
    private String token;
    private String newPassword;
}
