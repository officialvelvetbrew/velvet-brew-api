ALTER TABLE orders
    ADD COLUMN user_id BIGINT REFERENCES users(id);

CREATE INDEX idx_orders_user_id ON orders(user_id);
