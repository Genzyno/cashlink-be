package com.john.ledger.common.util;

import io.swagger.v3.oas.annotations.media.Schema;


@Schema(name = "ServiceResponse", description = "Standard API response wrapper")
public class ServiceResponse<T> {

    @Schema(description = "HTTP status code", example = "201")
    private int statusCode;

    @Schema(description = "Response message", example = "Customer created successfully")
    private String message;

    @Schema(description = "Actual response payload")
    private T data;

    public static <T> ServiceResponse<T> successResponse(int statusCode, String message, T data) {
        return new ServiceResponse<>(statusCode, message, data);
    }

    public static <T> ServiceResponse<T> failureResponse(int statusCode, String message) {
        return new ServiceResponse<>(statusCode, message, null);
    }

    public ServiceResponse(int statusCode, String message, T data) {
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
    }


    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(T data) {
        this.data = data;
    }
}
