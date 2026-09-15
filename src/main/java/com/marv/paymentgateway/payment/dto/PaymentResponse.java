package com.marv.paymentgateway.payment.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PaymentResponse(

        UUID paymentReference,
        String orderId,
        String customerId,
        Long amount,
        String currency,
        String status,

        OffsetDateTime createdAt,
        OffsetDateTime authorizedAt,
        OffsetDateTime capturedAt,
        OffsetDateTime voidedAt,
        OffsetDateTime refundedAt

) {
}
