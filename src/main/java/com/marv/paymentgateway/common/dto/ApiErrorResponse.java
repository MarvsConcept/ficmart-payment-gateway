package com.marv.paymentgateway.common.dto;

import java.time.OffsetDateTime;

public record ApiErrorResponse(

        String error,
        String message,
        OffsetDateTime timestamp
) {

}
