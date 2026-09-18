ALTER TABLE idempotency_records
ADD COLUMN payment_reference UUID;

ALTER TABLE idempotency_records
ADD CONSTRAINT fk_idempotency_payment
FOREIGN KEY (payment_reference)
REFERENCES payment_receipts(payment_reference);