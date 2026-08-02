CREATE TABLE payments
(
    id BIGSERIAL PRIMARY KEY,

    order_number VARCHAR(30) NOT NULL,

    razorpay_order_id VARCHAR(100),

    razorpay_payment_id VARCHAR(100),

    razorpay_signature TEXT,

    amount NUMERIC(10,2) NOT NULL,

    payment_status VARCHAR(20) NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);