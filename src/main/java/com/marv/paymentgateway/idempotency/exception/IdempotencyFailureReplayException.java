package com.marv.paymentgateway.idempotency.exception;

import com.marv.paymentgateway.common.dto.ApiErrorResponse;
import lombok.Getter;

@Getter
public class IdempotencyFailureReplayException extends RuntimeException{

    private final int statusCode;
    private final ApiErrorResponse response;

    public IdempotencyFailureReplayException(int statusCode, ApiErrorResponse response) {
        super(response.message());
        this.statusCode = statusCode;
        this.response = response;
    }
}
