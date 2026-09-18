package com.marv.paymentgateway.bank;

import com.marv.paymentgateway.bank.dto.*;
import com.marv.paymentgateway.bank.exception.PermanentBankException;
import com.marv.paymentgateway.bank.exception.TransientBankException;
import jakarta.transaction.TransactionalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.function.Supplier;

import static java.lang.Thread.sleep;

@Slf4j
@Component
public class BankClient {

    private static final int MAX_ATTEMPTS = 3;
    private static final long INITIAL_BACKOFF_MS  = 250;

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
        return executeBankCall(() ->
                restClient.post()
                .uri("/api/v1/authorizations")
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(BankAuthorizationResponse.class));

    }

    public BankCaptureResponse capture(
            BankCaptureRequest request,
            String idempotencyKey) {

        return executeBankCall(() ->
                restClient.post()
                .uri("/api/v1/captures")
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(BankCaptureResponse.class));
    }

    public BankVoidResponse voidAuthorization(
            BankVoidRequest request,
            String idempotencyKey) {

        return executeBankCall(() ->
                restClient.post()
                .uri("api/v1/voids")
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(BankVoidResponse.class));
    }

    public BankRefundResponse refund(
            BankRefundRequest request,
            String idempotencyKey) {

        return executeBankCall(() ->
                restClient.post()
                .uri("api/v1/refunds")
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(BankRefundResponse.class));
    }


    private <T> T executeBankCall(Supplier<T> bankCall) {

        long backoffMs = INITIAL_BACKOFF_MS;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return bankCall.get();

            } catch (RestClientResponseException ex) {

                if (!ex.getStatusCode().is5xxServerError()) {
                    throw new PermanentBankException(
                            "Bank rejected the request",
                            ex);
                }

                if (attempt == MAX_ATTEMPTS) {
                    throw new TransientBankException(
                            "Bank failed after retry attempts",
                            ex);
                }
            } catch (ResourceAccessException ex) {
                if (attempt == MAX_ATTEMPTS) {
                    throw new TransientBankException(
                            "Bank could not be reached after retry attempts",
                            ex);
                }
            }

            log.warn(
                    "Transient bank failure on attempt {}/{}. Retrying in {} ms",
                    attempt,
                    MAX_ATTEMPTS,
                    backoffMs
            );
            // Back off before retrying so we do not hammer a struggling bank.
            sleep(backoffMs);
            backoffMs *= 2;
        }

        throw new IllegalStateException("Unreachable retry state");
    }

    private void sleep( long delayMs){
        try {
            Thread.sleep(delayMs);

        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();

            throw new TransientBankException(
                    "Bank retry was interrupted",
                    ex);
        }
    }

}
