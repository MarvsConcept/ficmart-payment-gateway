package com.marv.paymentgateway.bank.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record BankVoidResponse(

        @JsonProperty("authorization_id")
        String authorizationId,

        String status,

        @JsonProperty("void_id")
        String voidId,

        @JsonProperty("voided_at")
        OffsetDateTime voidedAt
) {
}
