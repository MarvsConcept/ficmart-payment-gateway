package com.marv.paymentgateway.idempotency;

import com.marv.paymentgateway.payment.dto.AuthorizePaymentRequest;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
public class RequestFingerprintService {

    public String forAuthorization(AuthorizePaymentRequest request) {

        String canonicalRequest = String.join("|",
                request.orderId(),
                request.customerId(),
                request.amount().toString(),
                request.cardNumber(),
                request.cvv(),
                request.expiryMonth().toString(),
                request.expiryYear().toString());

        try {

            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    canonicalRequest.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException ex) {
            // SHA-256 is required by the Java platform; absence is a runtime configuration failure.
            throw new IllegalStateException(
                    "SHA-256 algorithm is available",
                    ex
            );
        }
    }
}
