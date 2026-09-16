package com.marv.paymentgateway.payment;

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
                    idempotencyService.replay(
                            idempotencyRecord,
                            AuthorizePaymentResponse.class);

            return ResponseEntity
                    .status(idempotencyRecord.getHttpStatus())
                    .body(replayedResponse);
        }

        PaymentReceipt payment = paymentService.authorizePayment(request);

        AuthorizePaymentResponse response = toAuthorizePaymentResponse(payment);

        idempotencyService.complete(
                idempotencyRecord,
                response,
                HttpStatus.CREATED.value()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/{paymentReference}/capture")
    public ResponseEntity<CapturePaymentResponse> capture(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @PathVariable UUID paymentReference) {

        String requestHash = requestFingerprintService.forPaymentOperation(paymentReference);

        IdempotencyRecord record = idempotencyService.claim(
                idempotencyKey,
                IdempotencyOperation.CAPTURE,
                requestHash
        );

        if (record.getStatus() == IdempotencyStatus.COMPLETED) {

            CapturePaymentResponse response =
                    idempotencyService.replay(record, CapturePaymentResponse.class);

            return ResponseEntity
                    .status(record.getHttpStatus())
                    .body(response);

        }
        PaymentReceipt payment = paymentService.capturePayment(paymentReference);

        CapturePaymentResponse response = toCapturePaymentResponse(payment);

        idempotencyService.complete(
                record,
                response,
                HttpStatus.OK.value()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);

    }

    @PostMapping("/{paymentReference}/void")
    public ResponseEntity<VoidPaymentResponse> voidPayment(
            @PathVariable UUID paymentReference) {

        PaymentReceipt payment = paymentService.voidPayment(paymentReference);

        VoidPaymentResponse response = toVoidPaymentResponse(payment);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);

    }

    @PostMapping("/{paymentReference}/refund")
    public ResponseEntity<RefundPaymentResponse> refund(
            @PathVariable UUID paymentReference) {

        PaymentReceipt payment = paymentService.refundPayment(paymentReference);

        RefundPaymentResponse response = toRefundPaymentResponse(payment);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByorderId(
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
