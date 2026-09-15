package com.marv.paymentgateway.common.exception;

import com.marv.paymentgateway.common.dto.ApiErrorResponse;
import com.marv.paymentgateway.payment.exception.InvalidPaymentStateException;
import com.marv.paymentgateway.payment.exception.PaymentNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handlePaymentNotFound (PaymentNotFoundException ex) {

        ApiErrorResponse response = new ApiErrorResponse(
                "payment_not_found",
                ex.getMessage(),
                OffsetDateTime.now()
        );

        return ResponseEntity.
                status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(InvalidPaymentStateException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidPaymentState (InvalidPaymentStateException ex) {

        ApiErrorResponse response = new ApiErrorResponse(
                "invalid_payment_state",
                ex.getMessage(),
                OffsetDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }
}
