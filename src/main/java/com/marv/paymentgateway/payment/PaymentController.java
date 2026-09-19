package com.marv.paymentgateway.payment;

import com.marv.paymentgateway.bank.exception.PermanentBankException;
import com.marv.paymentgateway.bank.exception.TransientBankException;
import com.marv.paymentgateway.common.dto.ApiErrorResponse;
import com.marv.paymentgateway.idempotency.*;
import com.marv.paymentgateway.payment.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final IdempotencyService idempotencyService;
    private final RequestFingerprintService requestFingerprintService;

    @PostMapping("/authorize")
    public ResponseEntity<AuthorizePaymentResponse> authorize(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody AuthorizePaymentRequest request) {

        String requestHash = requestFingerprintService.forAuthorization(request);

        IdempotencyRecord idempotencyRecord = idempotencyService.claim(
                idempotencyKey,
                IdempotencyOperation.AUTHORIZE,
                requestHash
        );

        if (idempotencyRecord.getStatus() == IdempotencyStatus.COMPLETED) {

            AuthorizePaymentResponse replayedResponse =
                    idempotencyService.replay(idempotencyRecord, AuthorizePaymentResponse.class);
            return ResponseEntity
                    .status(idempotencyRecord.getHttpStatus())
                    .body(replayedResponse);
        }

        if (idempotencyRecord.getStatus() == IdempotencyStatus.FAILED) {
            idempotencyService.replayFailure(idempotencyRecord);
        }

        PaymentReceipt payment; // = paymentService.authorizePayment(request);
        if (idempotencyRecord.getPaymentReference() == null) {

            payment = paymentService.createPendingPayment(request);

            // The idempotency key must remain tied to this same payment on every retry.
            idempotencyService.attachPayment(idempotencyRecord, payment.getPaymentReference());
        } else {
            payment = paymentService.getPayment(
                    idempotencyRecord.getPaymentReference());
        }

        try {
            PaymentReceipt authorizedPayment =
                    paymentService.authorizePendingPayment(payment, request);

            AuthorizePaymentResponse response = toAuthorizePaymentResponse(payment);

            idempotencyService.complete(
                    idempotencyRecord,
                    response,
                    HttpStatus.CREATED.value());

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);
        } catch (TransientBankException ex) {
            idempotencyService.markRetryable(idempotencyRecord);
            throw ex;
        } catch (PermanentBankException ex) {

            payment.markFailed();
            paymentService.save(payment);

            ApiErrorResponse errorResponse = new ApiErrorResponse(
                    ex.getErrorCode(),
                    ex.getMessage(),
                    OffsetDateTime.now());

            // Permanent failures must replay rather than re-run the bank operation.
            idempotencyService.fail(
                    idempotencyRecord,
                    errorResponse,
                    ex.getStatusCode());

            throw ex;

        }
    }

    @PostMapping("/{paymentReference}/capture")
    public ResponseEntity<CapturePaymentResponse> capture(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @PathVariable UUID paymentReference) {

        String requestHash = requestFingerprintService.forPaymentOperation(paymentReference);

        IdempotencyRecord idempotencyRecord = idempotencyService.claim(
                idempotencyKey,
                IdempotencyOperation.CAPTURE,
                requestHash
        );

        if (idempotencyRecord.getStatus() == IdempotencyStatus.COMPLETED) {
            CapturePaymentResponse replayedResponse =
                    idempotencyService.replay(idempotencyRecord, CapturePaymentResponse.class);
            return ResponseEntity
                    .status(idempotencyRecord.getHttpStatus())
                    .body(replayedResponse);
        }

        if (idempotencyRecord.getStatus() == IdempotencyStatus.FAILED) {
            idempotencyService.replayFailure(idempotencyRecord);
        }

        try {
            PaymentReceipt payment = paymentService.capturePayment(paymentReference);

            CapturePaymentResponse response = toCapturePaymentResponse(payment);

            idempotencyService.complete(
                idempotencyRecord,
                response,
                HttpStatus.OK.value()
            );

            return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);

        } catch (TransientBankException ex) {

            // A temporary bank failure can safely resume with the same operation key.
            idempotencyService.markRetryable(idempotencyRecord);
            throw ex;
        } catch (PermanentBankException ex) {
            ApiErrorResponse errorResponse = new ApiErrorResponse(
                    ex.getErrorCode(),
                    ex.getMessage(),
                    OffsetDateTime.now());

            // Permanent failures must replay rather than calling the bank operation again.
            idempotencyService.fail(
                    idempotencyRecord,
                    errorResponse,
                    ex.getStatusCode());

            throw ex;
        }
    }

    @PostMapping("/{paymentReference}/void")
    public ResponseEntity<VoidPaymentResponse> voidPayment(
            @RequestHeader("Idempotency_key") String idempotencyKey,
            @PathVariable UUID paymentReference) {

        String requestHash = requestFingerprintService.forPaymentOperation(paymentReference);

        IdempotencyRecord idempotencyRecord = idempotencyService.claim(
                idempotencyKey,
                IdempotencyOperation.VOID,
                requestHash);

        if (idempotencyRecord.getStatus() == IdempotencyStatus.COMPLETED) {
            VoidPaymentResponse replayedResponse =
                    idempotencyService.replay(idempotencyRecord, VoidPaymentResponse.class);

            return ResponseEntity
                    .status(idempotencyRecord.getHttpStatus())
                    .body(replayedResponse);
        }
        if (idempotencyRecord.getStatus() == IdempotencyStatus.FAILED) {
            idempotencyService.replayFailure(idempotencyRecord);
        }

        try {

            PaymentReceipt payment = paymentService.voidPayment(paymentReference);

            VoidPaymentResponse response = toVoidPaymentResponse(payment);

            idempotencyService.complete(
                    idempotencyRecord,
                    response,
                    HttpStatus.OK.value());

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(response);

        } catch (TransientBankException ex) {

            // A temporary bank failure can safely resume with the same operation key.
            idempotencyService.markRetryable(idempotencyRecord);
            throw ex;
        } catch (PermanentBankException ex) {
            ApiErrorResponse errorResponse = new ApiErrorResponse(
                    ex.getErrorCode(),
                    ex.getMessage(),
                    OffsetDateTime.now());

            // Permanent failures must replay rather than calling the bank operation again.
            idempotencyService.fail(
                    idempotencyRecord,
                    errorResponse,
                    ex.getStatusCode());

            throw ex;
        }
    }

    @PostMapping("/{paymentReference}/refund")
    public ResponseEntity<RefundPaymentResponse> refund(
            @RequestHeader("Idempotency-key") String idempotencyKey,
            @PathVariable UUID paymentReference) {

        String requestHash = requestFingerprintService.forPaymentOperation(paymentReference);

        IdempotencyRecord idempotencyRecord = idempotencyService.claim(
                idempotencyKey,
                IdempotencyOperation.VOID,
                requestHash);

        if (idempotencyRecord.getStatus() == IdempotencyStatus.COMPLETED) {
            RefundPaymentResponse replayedResponse =
                    idempotencyService.replay(idempotencyRecord, RefundPaymentResponse.class);

            return ResponseEntity
                    .status(idempotencyRecord.getHttpStatus())
                    .body(replayedResponse);
        }
        if (idempotencyRecord.getStatus() == IdempotencyStatus.FAILED) {
            idempotencyService.replayFailure(idempotencyRecord);
        }

        try {
            PaymentReceipt payment = paymentService.refundPayment(paymentReference);

            RefundPaymentResponse response = toRefundPaymentResponse(payment);

            idempotencyService.complete(
                    idempotencyRecord,
                    response,
                    HttpStatus.OK.value());

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(response);
        } catch (TransientBankException ex) {

            // A temporary bank failure can safely resume with the same operation key.
            idempotencyService.markRetryable(idempotencyRecord);
            throw ex;
        } catch (PermanentBankException ex) {
            ApiErrorResponse errorResponse = new ApiErrorResponse(
                    ex.getErrorCode(),
                    ex.getMessage(),
                    OffsetDateTime.now());

            // Permanent failures must replay rather than calling the bank operation again.
            idempotencyService.fail(
                    idempotencyRecord,
                    errorResponse,
                    ex.getStatusCode());

            throw ex;
        }
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(
            @PathVariable String orderId) {

        PaymentReceipt payment = paymentService.getPaymentByOrderId(orderId);

        PaymentResponse response = toPaymentResponse(payment);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/customers/{customerId}")
    public ResponseEntity<List<PaymentResponse>> getCustomerPaymentHistory(
            @PathVariable String customerId) {

        List<PaymentReceipt> payments = paymentService.getCustomerPaymentHistory(customerId);

        List<PaymentResponse> response = payments.stream()
                .map(this::toPaymentResponse)
                .toList();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    private PaymentResponse toPaymentResponse(
            PaymentReceipt payment) {

        return new PaymentResponse(

                payment.getPaymentReference(),
                payment.getOrderId(),
                payment.getCustomerId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus().name(),

                payment.getCreatedAt(),
                payment.getAuthorizedAt(),
                payment.getCapturedAt(),
                payment.getVoidedAt(),
                payment.getRefundedAt()
        );
    }

    private RefundPaymentResponse toRefundPaymentResponse (
            PaymentReceipt payment) {

        return new RefundPaymentResponse(

                payment.getPaymentReference(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus().name(),
                payment.getRefundedAt()
        );
    }

    private VoidPaymentResponse toVoidPaymentResponse (
            PaymentReceipt payment) {

        return new VoidPaymentResponse(
                payment.getPaymentReference(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus().name(),
                payment.getVoidedAt()
        );
    }

    private  CapturePaymentResponse toCapturePaymentResponse(
            PaymentReceipt payment) {

        return new CapturePaymentResponse(
                payment.getPaymentReference(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus().name(),
                payment.getCapturedAt()
        );
    }

    private AuthorizePaymentResponse toAuthorizePaymentResponse(
            PaymentReceipt payment) {

        return new AuthorizePaymentResponse(
                payment.getPaymentReference(),
                payment.getOrderId(),
                payment.getCustomerId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus().name(),
                payment.getAuthorizedAt()
        );
    }
}
