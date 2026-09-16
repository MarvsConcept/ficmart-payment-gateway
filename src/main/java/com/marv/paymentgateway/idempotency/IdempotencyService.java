package com.marv.paymentgateway.idempotency;

import com.marv.paymentgateway.idempotency.exception.IdempotencyConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final IdempotencyRecordRepository repository;

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

        return record;
    }
}
