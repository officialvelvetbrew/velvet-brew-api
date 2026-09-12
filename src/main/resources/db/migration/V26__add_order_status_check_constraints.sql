-- order_status/payment_status were plain VARCHAR(20) with nothing stopping an
-- invalid value being written (this is exactly how 'REJECTED' ended up in
-- orders.order_status with no matching Java enum constant, taking down
-- GET /api/v1/customer/orders entirely - Hibernate can't hydrate a row whose
-- enum column holds a value the enum doesn't define).
--
-- Added NOT VALID deliberately: this skips checking EXISTING rows, so it
-- cannot fail this migration against data we haven't inspected. It still
-- enforces the constraint for every INSERT/UPDATE from this point on. Once
-- you've confirmed (see below) there's no other bad data, run:
--   ALTER TABLE orders VALIDATE CONSTRAINT chk_orders_order_status;
--   ALTER TABLE orders VALIDATE CONSTRAINT chk_orders_payment_status;
--   ALTER TABLE payments VALIDATE CONSTRAINT chk_payments_payment_status;
-- to have Postgres check the backlog too (a one-time read-only scan, safe to
-- run any time).
--
-- To check for other bad values yourself first:
--   SELECT DISTINCT order_status FROM orders WHERE order_status NOT IN
--     ('PENDING','ACCEPTED','REJECTED','PREPARING','READY','COMPLETED','CANCELLED');
--   SELECT DISTINCT payment_status FROM orders WHERE payment_status NOT IN
--     ('PENDING','SUCCESS','FAILED','REFUNDED');
--   SELECT DISTINCT payment_status FROM payments WHERE payment_status NOT IN
--     ('PENDING','SUCCESS','FAILED','REFUNDED');

ALTER TABLE orders
    ADD CONSTRAINT chk_orders_order_status
    CHECK (order_status IN ('PENDING','ACCEPTED','REJECTED','PREPARING','READY','COMPLETED','CANCELLED'))
    NOT VALID;

ALTER TABLE orders
    ADD CONSTRAINT chk_orders_payment_status
    CHECK (payment_status IN ('PENDING','SUCCESS','FAILED','REFUNDED'))
    NOT VALID;

ALTER TABLE payments
    ADD CONSTRAINT chk_payments_payment_status
    CHECK (payment_status IN ('PENDING','SUCCESS','FAILED','REFUNDED'))
    NOT VALID;
