CREATE TABLE order_items
(
    id BIGSERIAL PRIMARY KEY,

    order_id BIGINT NOT NULL,

    menu_item_id BIGINT NOT NULL,

    quantity INT NOT NULL,

    unit_price NUMERIC(10,2) NOT NULL,

    total_price NUMERIC(10,2) NOT NULL,

    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id)
        REFERENCES orders(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_order_items_menu
        FOREIGN KEY (menu_item_id)
        REFERENCES menu_items(id)
);