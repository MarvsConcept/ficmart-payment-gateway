package com.marv.paymentgateway.idempotency;

public enum IdempotencyOperation {

    AUTHORIZE,
    CAPTURE,
    VOID,
    REFUND
}
