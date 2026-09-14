package com.marv.paymentgateway.payment;

import com.marv.paymentgateway.payment.dto.AuthorizePaymentRequest;
import com.marv.paymentgateway.payment.dto.AuthorizePaymentResponse;
import com.marv.paymentgateway.payment.dto.CapturePaymentResponse;
import com.marv.paymentgateway.payment.dto.VoidPaymentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/authorize")
    public ResponseEntity<AuthorizePaymentResponse> authorize(
            @Valid @RequestBody AuthorizePaymentRequest request) {

        PaymentReceipt payment = paymentService.authorizePayment(request);

        AuthorizePaymentResponse response = toAuthorizePaymentResponse(payment);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/{paymentReference}/capture")
    public ResponseEntity<CapturePaymentResponse> capture(
            @PathVariable UUID paymentReference) {

        PaymentReceipt payment = paymentService.capturePayment(paymentReference);

        CapturePaymentResponse response = toCapturePaymentResponse(payment);

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
