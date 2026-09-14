package com.marv.paymentgateway.bank.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record BankRefundResponse(

        Long amount,

        @JsonProperty("Capture_id")
        String captureId,

        String currency,

        @JsonProperty("refund_id")
        String refundId,

        @JsonProperty("refunded_at")
        OffsetDateTime refundedAt,

        String status
) {
}
