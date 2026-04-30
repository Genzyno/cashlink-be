package com.john.ledger.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerAuthTokenResponse {
    private String accessToken;
    private String refreshToken;
    private Integer expiresIn;
    private String tokenType;
    private LedgerAuthUserInfo user;
}
