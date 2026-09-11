CREATE TABLE payment_receipts (
    payment_reference UUID PRIMARY KEY,

    order_id VARCHAR(100) NOT NULL,
    customer_id VARCHAR(100) NOT NULL,

    amount BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(30) NOT NULL,

    authorization_id VARCHAR(150),
    capture_id VARCHAR(150),
    void_id VARCHAR(150),
    refund_id VARCHAR(150),

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    authorized_at TIMESTAMP WITH TIME ZONE,
    captured_at TIMESTAMP WITH TIME ZONE,
    voided_at TIMESTAMP WITH TIME ZONE,
    refunded_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);