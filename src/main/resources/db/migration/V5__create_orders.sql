CREATE TABLE orders
(
    id BIGSERIAL PRIMARY KEY,

    order_number VARCHAR(30) NOT NULL UNIQUE,

    customer_id BIGINT NOT NULL,

    subtotal NUMERIC(10,2) NOT NULL,

    tax NUMERIC(10,2) DEFAULT 0,

    discount NUMERIC(10,2) DEFAULT 0,

    total_amount NUMERIC(10,2) NOT NULL,

    payment_status VARCHAR(20) NOT NULL,

    order_status VARCHAR(20) NOT NULL,

    special_instructions TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_orders_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(id)
);