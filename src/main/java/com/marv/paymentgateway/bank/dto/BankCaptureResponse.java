package com.marv.paymentgateway.bank.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record BankCaptureResponse(

        Long amount,

        @JsonProperty("authorization_id")
        String authorizationId,

        @JsonProperty("capture_id")
        String captureId,

        @JsonProperty("captured_at")
        OffsetDateTime capturedAt,

        String currency,

        String status
) {
}
