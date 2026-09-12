package com.marv.paymentgateway.payment;

import com.marv.paymentgateway.payment.dto.AuthorizePaymentRequest;
import com.marv.paymentgateway.payment.dto.AuthorizePaymentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
