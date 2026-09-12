CREATE TABLE offers
(
    id BIGSERIAL PRIMARY KEY,

    code VARCHAR(30) NOT NULL UNIQUE,

    name VARCHAR(150) NOT NULL,

    description VARCHAR(255),

    discount_type VARCHAR(20) NOT NULL,

    discount_value NUMERIC(10,2) NOT NULL,

    max_discount_amount NUMERIC(10,2),

    min_order_amount NUMERIC(10,2) NOT NULL DEFAULT 0,

    starts_at TIMESTAMP,

    ends_at TIMESTAMP,

    max_uses_total INT,

    max_uses_per_customer INT,

    current_uses INT NOT NULL DEFAULT 0,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_offers_discount_type CHECK (discount_type IN ('PERCENTAGE', 'FLAT')),
    CONSTRAINT chk_offers_discount_value CHECK (discount_value > 0),
    CONSTRAINT chk_offers_min_order_amount CHECK (min_order_amount >= 0),
    CONSTRAINT chk_offers_current_uses CHECK (current_uses >= 0)
);

CREATE INDEX idx_offers_code ON offers (code);
CREATE INDEX idx_offers_active ON offers (active);
