CREATE TABLE offer_redemptions
(
    id BIGSERIAL PRIMARY KEY,

    offer_id BIGINT NOT NULL,

    customer_id BIGINT NOT NULL,

    order_number VARCHAR(30) NOT NULL,

    discount_amount NUMERIC(10,2) NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_offer_redemptions_offer
        FOREIGN KEY (offer_id) REFERENCES offers(id) ON DELETE CASCADE,

    CONSTRAINT fk_offer_redemptions_customer
        FOREIGN KEY (customer_id) REFERENCES customers(id)
);

CREATE INDEX idx_offer_redemptions_offer_id ON offer_redemptions (offer_id);
CREATE INDEX idx_offer_redemptions_customer_id ON offer_redemptions (customer_id);
CREATE INDEX idx_offer_redemptions_order_number ON offer_redemptions (order_number);
