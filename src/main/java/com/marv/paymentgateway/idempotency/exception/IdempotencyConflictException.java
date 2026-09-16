package com.marv.paymentgateway.idempotency.exception;

public class IdempotencyConflictException extends RuntimeException{

    public IdempotencyConflictException(String message) {
        super(message);
    }
}
