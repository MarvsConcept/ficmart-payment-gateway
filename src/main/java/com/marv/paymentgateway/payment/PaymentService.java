package com.marv.paymentgateway.payment;

import com.marv.paymentgateway.payment.dto.AuthorizePaymentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentReceiptRepository paymentReceiptRepository;

    public PaymentReceipt createPendingPayment(
            AuthorizePaymentRequest request
    ) {

        PaymentReceipt payment = PaymentReceipt.createPending(
                request.orderId(),
                request.customerId(),
                request.amount()
        );

        return paymentReceiptRepository.save(payment);
    }
}
