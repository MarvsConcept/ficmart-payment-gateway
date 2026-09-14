package com.marv.paymentgateway.bank.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BankRefundRequest(

        Long amount,

        @JsonProperty("capture_id")
        String captureId
) {
}
