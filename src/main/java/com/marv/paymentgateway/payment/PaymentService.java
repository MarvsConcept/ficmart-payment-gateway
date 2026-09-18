package com.marv.paymentgateway.payment;

import com.marv.paymentgateway.bank.BankClient;
import com.marv.paymentgateway.bank.dto.*;
import com.marv.paymentgateway.idempotency.IdempotencyRecord;
import com.marv.paymentgateway.payment.dto.AuthorizePaymentRequest;
import com.marv.paymentgateway.payment.exception.PaymentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentReceiptRepository paymentReceiptRepository;
    private final BankClient bankClient;

    public PaymentReceipt createPendingPayment(
            AuthorizePaymentRequest request) {

        PaymentReceipt payment = PaymentReceipt.createPending(
                request.orderId(),
                request.customerId(),
                request.amount());

        // Save before calling the bank to leave pending record that can be reconciled
        return paymentReceiptRepository.save(payment);
    }

    public PaymentReceipt getPayment(UUID paymentReference) {
        return paymentReceiptRepository.findById(paymentReference)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentReference));
    }

    public PaymentReceipt authorizePendingPayment(
            PaymentReceipt payment,
        AuthorizePaymentRequest request) {

        // reject an invalid state before calling the bank
        payment.ensureCanBeAuthorized();

        // Generate idempotency key with the payment reference
        String bankIdempotencyKey = "authorize:" + payment.getPaymentReference();

        // Convert FicMarts request to the bank request
        BankAuthorizationRequest bankRequest = toBankAuthorizationRequest(request);

        // Call the Bank
        BankAuthorizationResponse bankResponse =
                bankClient.authorize(bankRequest, bankIdempotencyKey);

        payment.markAuthorized(bankResponse.authorizationId(), bankResponse.createdAt());

        return paymentReceiptRepository.save(payment);
    }

    public PaymentReceipt capturePayment(UUID paymentReference) {

        // find payment in the db with reference
        PaymentReceipt payment = paymentReceiptRepository.findById(paymentReference).
                orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentReference));


        // reject an invalid capture before calling the bank
        payment.ensureCanBeCaptured();

        // Build the bank capture request
        BankCaptureRequest bankRequest = new BankCaptureRequest(
                payment.getAmount(),
                payment.getAuthorizationId()
        );

        // Generate idempotency key with the payment reference
        String bankIdempotencyKey = "capture:" + payment.getPaymentReference();

        // Call the bank
        BankCaptureResponse bankResponse =
                bankClient.capture(bankRequest, bankIdempotencyKey);

        // call markCaptured
        payment.markCaptured(bankResponse.captureId(), bankResponse.capturedAt());

        return paymentReceiptRepository.save(payment);
    }

    public PaymentReceipt voidPayment(UUID paymentReference) {

        // find payment in the db with reference
        PaymentReceipt payment = paymentReceiptRepository.findById(paymentReference)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentReference));

        // reject an invalid transition before calling the bank
        payment.ensureCanBeVoided();

        // Build the bank capture request
        BankVoidRequest bankRequest = new BankVoidRequest(
                payment.getAuthorizationId()
        );

        // Generate idempotency key
        String bankIdempotencyKey = "void:" + payment.getPaymentReference();

        // Call the bank
        BankVoidResponse bankResponse =
                bankClient.voidAuthorization(bankRequest, bankIdempotencyKey);

        // call markVoided
        payment.markVoided(bankResponse.voidId(), bankResponse.voidedAt());

        return paymentReceiptRepository.save(payment);
    }

    public PaymentReceipt refundPayment(UUID paymentReference) {

        // find payment in the db with reference
        PaymentReceipt payment = paymentReceiptRepository.findById(paymentReference)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentReference));

        // ensure payment is captured and can be refunded
        payment.ensureCanBeRefunded();

        // Build the bank refund request
        BankRefundRequest bankRequest = new BankRefundRequest(
                payment.getAmount(), payment.getCaptureId());

        // Generate idempotency key
        String bankIdempotencyKey = "refund:" + payment.getPaymentReference();

        // Call the bank
        BankRefundResponse bankResponse = bankClient.refund(
                bankRequest, bankIdempotencyKey);

        // mark refunded
        payment.markRefunded(bankResponse.refundId(), bankResponse.refundedAt());

        return paymentReceiptRepository.save(payment);

    }

    private BankAuthorizationRequest toBankAuthorizationRequest(
            AuthorizePaymentRequest request) {

        return new BankAuthorizationRequest(
                request.amount(),
                request.cardNumber(),
                request.cvv(),
                request.expiryMonth(),
                request.expiryYear()
        );
    }

    public PaymentReceipt getPaymentByOrderId(String orderId) {

        return paymentReceiptRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + orderId));
    }

    public List<PaymentReceipt> getCustomerPaymentHistory(String customerId) {

        return paymentReceiptRepository.
                findAllByCustomerIdOrderByCreatedAtDesc(customerId);
    }

}
