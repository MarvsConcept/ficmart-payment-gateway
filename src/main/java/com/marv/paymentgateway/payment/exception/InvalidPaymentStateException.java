package com.marv.paymentgateway.payment.exception;

public class InvalidPaymentStateException extends RuntimeException{

    public InvalidPaymentStateException(String message) {
        super(message);
    }
}
