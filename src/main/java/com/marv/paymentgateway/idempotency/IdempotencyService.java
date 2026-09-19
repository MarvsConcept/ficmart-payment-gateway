package com.marv.paymentgateway.idempotency;

import com.marv.paymentgateway.common.dto.ApiErrorResponse;
import com.marv.paymentgateway.idempotency.exception.IdempotencyConflictException;
import com.marv.paymentgateway.idempotency.exception.IdempotencyFailureReplayException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final IdempotencyRecordRepository repository;
    private final JsonMapper jsonMapper;

    public IdempotencyRecord claim(
            String key,
            IdempotencyOperation operation,
            String requestHash) {

        return repository.findByIdempotencyKeyAndOperation(key, operation)
                .map(existing -> validateExisting(existing, requestHash))
                .orElseGet(() -> createRecord(key, operation, requestHash));
    }

    private IdempotencyRecord createRecord(
            String key,
            IdempotencyOperation operation,
            String requestHash) {

        try {
            return repository.saveAndFlush(
                    IdempotencyRecord.start(key, operation, requestHash));
        } catch (DataIntegrityViolationException ex) {
            //Another request may have claimed the same key concurrently.
            IdempotencyRecord existing = repository
                    .findByIdempotencyKeyAndOperation(key, operation)
                    .orElseThrow(() -> ex);

            return validateExisting(existing, requestHash);
        }
    }

    private IdempotencyRecord validateExisting(
            IdempotencyRecord record,
            String requestHash) {

        if (!record.getRequestHash().equals(requestHash)) {
            throw new IdempotencyConflictException(
                    "Idempotency key was already used with a different request");
        }
        if (record.getStatus() == IdempotencyStatus.IN_PROGRESS) {
            throw new IdempotencyConflictException("A request with this idempotency key is already in progress");
        }
        if (record.getStatus() == IdempotencyStatus.RETRYABLE) {
            record.markInProgress();
            return repository.save(record);
        }

        return record;
    }

    public void complete(
            IdempotencyRecord record,
            Object response,
            int httpStatus) {

        try {
            String responseBody = jsonMapper.writeValueAsString(response);

            record.complete(responseBody, httpStatus);
            repository.save(record);
        } catch (JacksonException ex) {
            throw new IllegalStateException("Failed to store idempotent response", ex);
        }
    }

    public void fail(
            IdempotencyRecord record,
            Object response,
            int httpStatus) {

        try {
            String responseBody = jsonMapper.writeValueAsString(response);

            record.fail(responseBody, httpStatus);
            repository.save(record);
        } catch (JacksonException ex) {
            throw new IllegalStateException("Failed to store idempotent response", ex);
        }
    }

    public <T> T replay(
            IdempotencyRecord record,
            Class<T> responseType) {

        try {
            return jsonMapper.readValue(
                    record.getResponseBody(),
                    responseType);
        } catch (JacksonException ex) {
            throw new IllegalStateException("Failed to replay idempotent response", ex);
        }
    }

    public void replayFailure(IdempotencyRecord record) {

        ApiErrorResponse response = replay(
                record, ApiErrorResponse.class);

        throw new IdempotencyFailureReplayException(
                record.getHttpStatus(), response);
    }

    public void attachPayment(
            IdempotencyRecord record,
            UUID paymentReference) {

        record.attachPayment(paymentReference);
        repository.save(record);
    }

    public void markRetryable(IdempotencyRecord record) {
        record.markRetryable();
        repository.save(record);
    }
}
