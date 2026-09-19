package com.marv.paymentgateway.idempotency;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "idempotency_records")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "payment_reference")
    private UUID paymentReference;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(name = "idempotency_key", nullable = false, length = 255)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private IdempotencyOperation operation;

    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private IdempotencyStatus status;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "http_status")
    private Integer httpStatus;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;


    public void attachPayment(UUID paymentReference) {
        if (this.paymentReference != null) {
            throw new IllegalStateException("Idempotency record already has a payment");
        }

        this.paymentReference = paymentReference;
        this.createdAt = OffsetDateTime.now();
    }

    public void markRetryable() {
        this.status = IdempotencyStatus.RETRYABLE;
        this.updatedAt = OffsetDateTime.now();
    }


    public void markInProgress() {
        if (status != IdempotencyStatus.RETRYABLE) {
            throw new IllegalStateException("Only retryable requests can be resumed");
        }

        this.status = IdempotencyStatus.IN_PROGRESS;
        this.updatedAt = OffsetDateTime.now();
    }

    public static IdempotencyRecord start(
            String idempotencyKey,
            IdempotencyOperation operation,
            String requestHash ) {

        IdempotencyRecord record = new IdempotencyRecord();
        OffsetDateTime now = OffsetDateTime.now();

        record.idempotencyKey = idempotencyKey;
        record.operation = operation;
        record.requestHash = requestHash;
        record.status = IdempotencyStatus.IN_PROGRESS;
        record.createdAt = now;
        record.updatedAt = now;

        return record;
    }

    public void complete(String responseBody, int httpStatus) {

        this.responseBody = responseBody;
        this.httpStatus = httpStatus;
        this.status = IdempotencyStatus.COMPLETED;
        this.updatedAt = OffsetDateTime.now();
    }

    public void fail(String responseBody, int httpStatus) {

        this.responseBody = responseBody;
        this.httpStatus = httpStatus;
        this.status = IdempotencyStatus.FAILED;
        this.updatedAt = OffsetDateTime.now();
    }

    public void reclaim() {

        if (status != IdempotencyStatus.IN_PROGRESS) {
            throw new IllegalStateException("Only in-progress requests can be reclaimed");
        }

        this.updatedAt = OffsetDateTime.now();
    }
}
