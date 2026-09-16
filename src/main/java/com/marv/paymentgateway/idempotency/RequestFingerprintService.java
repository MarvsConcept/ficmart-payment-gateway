package com.marv.paymentgateway.idempotency;

import com.marv.paymentgateway.payment.dto.AuthorizePaymentRequest;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class RequestFingerprintService {

    public String forAuthorization(AuthorizePaymentRequest request) {

        String cardNumber = request.cardNumber();
        String lastFour = cardNumber.length() <= 4
                ? cardNumber
                :cardNumber.substring(cardNumber.length() - 4);

        String canonicalRequest = String.join("|",
                request.orderId(),
                request.customerId(),
                request.amount().toString(),
                lastFour,
                request.expiryMonth().toString(),
                request.expiryYear().toString());


        return sha256(canonicalRequest);
    }

    public String forPaymentOperation(UUID paymentReference) {
        return sha256(paymentReference.toString());
    }

    private String sha256(String value) {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    value.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            // SHA-256 should always exist in a standard Java runtime.
            throw new IllegalStateException(
                    "SHA-256 algorithm is unavailable",
                    ex
            );
        }
    }
}
