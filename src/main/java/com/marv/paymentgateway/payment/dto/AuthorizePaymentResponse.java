package com.marv.paymentgateway.payment.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AuthorizePaymentResponse(

        UUID paymentReference,
        String orderId,
        String customerId,
        Long amount,
        String currency,
        String status,
        OffsetDateTime authorizedAt
) {
}
