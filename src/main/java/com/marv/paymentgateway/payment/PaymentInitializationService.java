package com.marv.paymentgateway.payment;

import com.marv.paymentgateway.idempotency.IdempotencyRecord;
import com.marv.paymentgateway.idempotency.IdempotencyRecordRepository;
import com.marv.paymentgateway.payment.dto.AuthorizePaymentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Service
@RequiredArgsConstructor
public class PaymentInitializationService {

    private final PaymentReceiptRepository paymentReceiptRepository;
    private final IdempotencyRecordRepository idempotencyRecordRepository;

    @Transactional
    public PaymentReceipt createPendingAndAttach(
            AuthorizePaymentRequest request,
            IdempotencyRecord idempotencyRecord) {

        PaymentReceipt payment = PaymentReceipt.createPending(
                request.orderId(),
                request.customerId(),
                request.amount());

        payment = paymentReceiptRepository.save(payment);

        // Both records must commit together so retries always know their payment.
        idempotencyRecord.attachPayment(payment.getPaymentReference());
        idempotencyRecordRepository.save(idempotencyRecord);

        return payment;
    }
}
