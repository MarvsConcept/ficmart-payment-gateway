package com.marv.paymentgateway.payment.dto;

import jakarta.validation.constraints.*;

public record AuthorizePaymentRequest(

        @NotBlank
        String orderId,

        @NotBlank
        String customerId,

        @NotNull
        @Positive
        Long amount,

        @NotBlank
        String cvv,

        @Min(1)
        @Max(12)
        int expiryMonth,

        @Min(2026)
        int expiryYear

) {
}
