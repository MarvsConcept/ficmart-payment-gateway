package com.marv.paymentgateway.bank;

import com.marv.paymentgateway.bank.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class BankClient {

    private final RestClient restClient;

    public BankClient(
            RestClient.Builder restClientBuilder,
            @Value("${bank.base-url}") String bankBaseUrl)
    {
        this.restClient = restClientBuilder
                .baseUrl(bankBaseUrl)
                .build();
    }

    public BankAuthorizationResponse authorize(
            BankAuthorizationRequest request,
            String idempotencyKey) {

        // The same key must be reused if this bank operation is retried
        // else the bank could treat the retry as a new authorization
        return restClient.post()
                .uri("/api/v1/authorizations")
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(BankAuthorizationResponse.class);

    }

    public BankCaptureResponse capture(
            BankCaptureRequest request,
            String idempotencyKey) {

        return restClient.post()
                .uri("/api/v1/captures")
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(BankCaptureResponse.class);
    }

    public BankVoidResponse voidAuthorization(
            BankVoidRequest request,
            String idempotencyKey) {

        return restClient.post()
                .uri("api/v1/voids")
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(BankVoidResponse.class);
    }

    public BankRefundResponse refund(
            BankRefundRequest request,
            String idempotencyKey) {

        return restClient.post()
                .uri("api/v1/refunds")
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(BankRefundResponse.class);
    }
}
