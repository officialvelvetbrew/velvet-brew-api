ALTER TABLE orders ADD COLUMN offer_id BIGINT REFERENCES offers(id);

CREATE INDEX idx_orders_offer_id ON orders (offer_id);
