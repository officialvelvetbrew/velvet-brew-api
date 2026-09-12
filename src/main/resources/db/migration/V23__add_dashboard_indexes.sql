CREATE INDEX idx_orders_created_at ON orders (created_at);

CREATE INDEX idx_orders_order_status ON orders (order_status);

CREATE INDEX idx_orders_payment_status ON orders (payment_status);

CREATE INDEX idx_order_items_menu_item_id ON order_items (menu_item_id);
