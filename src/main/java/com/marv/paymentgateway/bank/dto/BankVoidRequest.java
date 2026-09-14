package com.marv.paymentgateway.bank.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BankVoidRequest(

        @JsonProperty("authorization_id")
        String authorizationId
) {
}
