package com.john.ledger.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Result of Google OAuth callback: success (tokens), user not registered (redirect with error), or failure.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GoogleCallbackResult {
    public enum Status { SUCCESS, USER_NOT_REGISTERED, ERROR }

    private Status status;
    private LedgerAuthTokenResponse response;

    public static GoogleCallbackResult success(LedgerAuthTokenResponse response) {
        return new GoogleCallbackResult(Status.SUCCESS, response);
    }

    public static GoogleCallbackResult userNotRegistered() {
        return new GoogleCallbackResult(Status.USER_NOT_REGISTERED, null);
    }

    public static GoogleCallbackResult error() {
        return new GoogleCallbackResult(Status.ERROR, null);
    }
}
