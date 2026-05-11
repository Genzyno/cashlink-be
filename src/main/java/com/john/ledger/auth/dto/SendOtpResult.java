package com.john.ledger.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SendOtpResult {
    public enum Status { SUCCESS, INVALID_EMAIL, RATE_LIMITED, USER_NOT_FOUND, USER_ALREADY_EXISTS }

    private Status status;

    public static SendOtpResult success() {
        return new SendOtpResult(Status.SUCCESS);
    }

    public static SendOtpResult invalidEmail() {
        return new SendOtpResult(Status.INVALID_EMAIL);
    }

    public static SendOtpResult rateLimited() {
        return new SendOtpResult(Status.RATE_LIMITED);
    }

    public static SendOtpResult userNotFound() {
        return new SendOtpResult(Status.USER_NOT_FOUND);
    }

    public static SendOtpResult userAlreadyExists() {
        return new SendOtpResult(Status.USER_ALREADY_EXISTS);
    }
}
