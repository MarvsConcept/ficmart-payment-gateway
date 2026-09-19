package com.marv.paymentgateway.bank.dto;

public record BankErrorResponse(
        String error,
        String message)
{
}
