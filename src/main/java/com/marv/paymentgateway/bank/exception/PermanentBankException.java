package com.marv.paymentgateway.bank.exception;

public class PermanentBankException extends RuntimeException{

    public PermanentBankException(String message, Throwable cause) {
        super(message, cause);
    }
}
