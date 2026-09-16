CREATE TABLE idempotency_records (
    id UUID PRIMARY KEY,

    idempotency_key VARCHAR(255) NOT NULL,
    operation VARCHAR(30) NOT NULL,

    request_hash VARCHAR(64) NOT NULL,

    status VARCHAR(30) NOT NULL,

    response_body TEXT,
    http_status INTEGER,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT uk_idempotency_key_operation
        UNIQUE (idempotency_key, operation)
);