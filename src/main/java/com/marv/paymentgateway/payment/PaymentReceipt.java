package com.marv.paymentgateway.payment;

import com.marv.paymentgateway.payment.exception.InvalidPaymentStateException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment_receipts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payment_reference")
    private UUID paymentReference;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(name = "authorization_id")
    private String authorizationId;

    @Column(name = "capture_id")
    private String captureId;

    @Column(name = "void_id")
    private String voidId;

    @Column(name = "refund_id")
    private String refundId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "authorized_at")
    private OffsetDateTime authorizedAt;

    @Column(name = "captured_at")
    private OffsetDateTime capturedAt;

    @Column(name = "voided_at")
    private OffsetDateTime voidedAt;

    @Column(name = "refunded_at")
    private OffsetDateTime refundedAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;


    public static PaymentReceipt createPending(
            String orderId,
            String customerId,
            Long amount) {

        PaymentReceipt payment = new PaymentReceipt();

        OffsetDateTime now = OffsetDateTime.now();

        payment.orderId = orderId;
        payment.customerId = customerId;
        payment.amount = amount;
        payment.currency = "USD";
        payment.status = PaymentStatus.PENDING;
        payment.createdAt = now;
        payment.updatedAt = now;

        return payment;
    }

    public void markAuthorized(
            String authorizationId,
            OffsetDateTime authorizedAt) {

        if (status != PaymentStatus.PENDING) {
            throw new InvalidPaymentStateException("Only pending payments can be authorized");
        }

        this.authorizationId = authorizationId;
        this.authorizedAt = authorizedAt;
        this.status = PaymentStatus.AUTHORIZED;
        this.updatedAt = OffsetDateTime.now();
    }

    public void markCaptured(
            String captureId,
            OffsetDateTime capturedAt) {

        ensureCanBeCaptured();

        this.captureId = captureId;
        this.capturedAt = capturedAt;
        this.status = PaymentStatus.CAPTURED;
        this.updatedAt = OffsetDateTime.now();
    }

    public void markVoided(
            String voidId,
            OffsetDateTime voidedAt) {

        ensureCanBeVoided();

        this.voidId = voidId;
        this.voidedAt = voidedAt;
        this.status = PaymentStatus.VOIDED;
        this.updatedAt = OffsetDateTime.now();
    }

    public void markRefunded(
            String refundId,
            OffsetDateTime refundedAt) {

        ensureCanBeRefunded();

        this.refundId = refundId;
        this.refundedAt = refundedAt;
        this.status = PaymentStatus.REFUNDED;
        this.updatedAt = OffsetDateTime.now();
    }

    public void ensureCanBeCaptured() {
        if (status != PaymentStatus.AUTHORIZED) {
            throw new InvalidPaymentStateException("Only authorized payments can be captured");
        }
    }

    public void ensureCanBeVoided() {

        if (status != PaymentStatus.AUTHORIZED) {
            throw new InvalidPaymentStateException("Only pending payments can be voided");
        }
    }


    public void ensureCanBeRefunded() {
        if (status != PaymentStatus.CAPTURED) {
            throw new InvalidPaymentStateException("Only captured payments can be refunded");
        }
    }
}
