package com.john.ledger.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Result of verify-OTP: invalid OTP (400), user not registered (403), user already registered (409), or success (200 with tokens).
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOtpResult {
    public enum Status { INVALID_OTP, USER_NOT_REGISTERED, USER_ALREADY_REGISTERED, SUCCESS }

    private Status status;
    private LedgerAuthTokenResponse response;

    public static VerifyOtpResult invalidOtp() {
        return new VerifyOtpResult(Status.INVALID_OTP, null);
    }

    public static VerifyOtpResult userNotRegistered() {
        return new VerifyOtpResult(Status.USER_NOT_REGISTERED, null);
    }

    public static VerifyOtpResult userAlreadyRegistered() {
        return new VerifyOtpResult(Status.USER_ALREADY_REGISTERED, null);
    }


    public static VerifyOtpResult success(LedgerAuthTokenResponse response) {
        return new VerifyOtpResult(Status.SUCCESS, response);
    }
}
