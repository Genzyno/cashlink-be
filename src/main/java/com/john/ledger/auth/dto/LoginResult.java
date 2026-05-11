package com.john.ledger.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResult {
    public enum Status { SUCCESS, INVALID_CREDENTIALS, USER_NOT_FOUND, USER_INACTIVE }

    private Status status;
    private LedgerAuthTokenResponse response;

    public static LoginResult success(LedgerAuthTokenResponse response) {
        return new LoginResult(Status.SUCCESS, response);
    }

    public static LoginResult invalidCredentials() {
        return new LoginResult(Status.INVALID_CREDENTIALS, null);
    }

    public static LoginResult userNotFound() {
        return new LoginResult(Status.USER_NOT_FOUND, null);
    }

    public static LoginResult userInactive() {
        return new LoginResult(Status.USER_INACTIVE, null);
    }
}
