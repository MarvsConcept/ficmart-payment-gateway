package com.marv.paymentgateway.payment;

import com.marv.paymentgateway.bank.BankClient;
import com.marv.paymentgateway.bank.dto.BankAuthorizationRequest;
import com.marv.paymentgateway.bank.dto.BankAuthorizationResponse;
import com.marv.paymentgateway.payment.dto.AuthorizePaymentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentReceiptRepository paymentReceiptRepository;
    private final BankClient bankClient;

    public PaymentReceipt authorizePayment(
            AuthorizePaymentRequest request) {

        PaymentReceipt payment = PaymentReceipt.createPending(
                request.orderId(),
                request.customerId(),
                request.amount()
        );

        // Save before calling the bank to leave pending record that can be reconciled
        payment = paymentReceiptRepository.save(payment);

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


}
