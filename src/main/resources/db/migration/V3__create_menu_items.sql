CREATE TABLE menu_items (
    id BIGSERIAL PRIMARY KEY,

    category_id BIGINT NOT NULL,

    name VARCHAR(150) NOT NULL,

    description VARCHAR(500),

    price NUMERIC(10,2) NOT NULL,

    offer_price NUMERIC(10,2),

    image_url VARCHAR(500),

    veg BOOLEAN NOT NULL DEFAULT TRUE,

    available BOOLEAN NOT NULL DEFAULT TRUE,

    featured BOOLEAN NOT NULL DEFAULT FALSE,

    display_order INT NOT NULL DEFAULT 0,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_menu_category
        FOREIGN KEY(category_id)
        REFERENCES categories(id)
);