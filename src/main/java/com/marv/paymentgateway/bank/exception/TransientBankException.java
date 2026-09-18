package com.marv.paymentgateway.bank.exception;

public class TransientBankException extends RuntimeException{

    public TransientBankException(String message, Throwable cause) {
        super(message, cause);
    }
}
