package com.marv.paymentgateway.payment.exception;

public class PaymentNotFoundException extends RuntimeException{

    public PaymentNotFoundException(String message) {
        super(message);
    }
}
