-- Mobile is no longer mandatory for checkout/customer creation - a customer
-- can now be identified by email alone. The unique constraint on mobile is
-- unaffected: Postgres allows any number of NULLs in a unique column.
ALTER TABLE customers
    ALTER COLUMN mobile DROP NOT NULL;
