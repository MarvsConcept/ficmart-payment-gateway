package com.marv.paymentgateway.bank.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BankCaptureRequest(

        Long amount,

        @JsonProperty("authorization_id")
        String authorizationId
) {
}
