package com.marv.paymentgateway.idempotency;

public enum IdempotencyStatus {

    IN_PROGRESS,
    RETRYABLE,
    COMPLETED
}
