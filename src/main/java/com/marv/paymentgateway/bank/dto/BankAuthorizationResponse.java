package com.marv.paymentgateway.bank.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record BankAuthorizationResponse(

        Long amount,

        @JsonProperty("authorization_id")
        String authorizationId,

        @JsonProperty("created_at")
        OffsetDateTime createdAt,
        String currency,

        @JsonProperty("expires_at")
        OffsetDateTime expiresAt,
        String status
) {
}
