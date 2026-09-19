package com.marv.paymentgateway.bank.exception;

import lombok.Getter;

@Getter
public class PermanentBankException extends RuntimeException{

    private final int statusCode;
    private final String errorCode;
    private final String bankMessage;


    public PermanentBankException(
            int statusCode,
            String errorCode,
            String bankMessage,
            Throwable cause) {

        super(bankMessage, cause);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
        this.bankMessage = bankMessage;
    }
}
