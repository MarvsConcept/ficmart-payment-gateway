package com.marv.paymentgateway.common.exception;

import com.marv.paymentgateway.bank.exception.PermanentBankException;
import com.marv.paymentgateway.common.dto.ApiErrorResponse;
import com.marv.paymentgateway.idempotency.exception.IdempotencyConflictException;
import com.marv.paymentgateway.idempotency.exception.IdempotencyFailureReplayException;
import com.marv.paymentgateway.payment.exception.InvalidPaymentStateException;
import com.marv.paymentgateway.payment.exception.PaymentNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
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

    @ExceptionHandler(IdempotencyConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleIdempotencyConflictException(IdempotencyConflictException ex) {

        ApiErrorResponse response = new ApiErrorResponse(
                "idempotency_conflict",
                ex.getMessage(),
                OffsetDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiErrorResponse> handleOptimisticLock(
            ObjectOptimisticLockingFailureException ex) {

        ApiErrorResponse response = new ApiErrorResponse(
                "concurrent_update",
                "The payment was modified by another request. Retry the operation",
                OffsetDateTime.now());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    @ExceptionHandler(IdempotencyFailureReplayException.class)
    public ResponseEntity<ApiErrorResponse> handleIdempotentFailureReplay(
            IdempotencyFailureReplayException ex) {

        return ResponseEntity
                .status(ex.getStatusCode())
                .body(ex.getResponse());
    }

    @ExceptionHandler(PermanentBankException.class)
    public ResponseEntity<ApiErrorResponse> handlePermanentBankFailure(
            PermanentBankException ex
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                ex.getErrorCode(),
                ex.getBankMessage(),
                OffsetDateTime.now()
        );

        return ResponseEntity
                .status(ex.getStatusCode())
                .body(response);
    }
}
