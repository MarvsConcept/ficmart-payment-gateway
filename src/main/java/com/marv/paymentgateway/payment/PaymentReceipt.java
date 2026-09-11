package com.marv.paymentgateway.payment;

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
@Setter
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

}
