package com.marv.paymentgateway.payment.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CapturePaymentResponse(

        UUID paymentReference,
        String orderId,
        Long amount,
        String currency,
        String status,
        OffsetDateTime capturedAt
) {
}
