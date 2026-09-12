package com.marv.paymentgateway.bank.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BankAuthorizationRequest(

        Long amount,

        @JsonProperty("card_number")
        String cardNumber,

        String cvv,

        @JsonProperty("expiry_month")
        int expiryMonth,

        @JsonProperty("expiry_year")
        int expiryYear
) {
}
