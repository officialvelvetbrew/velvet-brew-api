-- Tracks whether an order's recipe ingredients have already been deducted
-- from inventory, so status/edit transitions know whether a restock is owed
-- without double-counting.
ALTER TABLE orders
    ADD COLUMN inventory_deducted BOOLEAN NOT NULL DEFAULT FALSE;
