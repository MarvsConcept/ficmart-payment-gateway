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
        String cardNumber,

        @NotBlank
        String cvv,

        @NotNull
        @Min(1)
        @Max(12)
        Integer expiryMonth,

        @NotNull
        @Min(2026)
        Integer expiryYear

) {
}
