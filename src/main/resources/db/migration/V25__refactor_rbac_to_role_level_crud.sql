-- Refactors RBAC permission granularity from "one function per CRUD subset"
-- (INVENTORY_ITEMS_MANAGE / INVENTORY_ITEMS_DELETE, etc.) to "one function per
-- module, with create/read/update/delete carried on the grant itself"
-- (role_functions). This matches what function_urls' can_create/read/update/
-- delete columns were already designed for, and removes the function
-- proliferation the V24 seed introduced as a workaround.

-- 1. Give each role->function grant its own CRUD flags.
ALTER TABLE role_functions
    ADD COLUMN can_create BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN can_read   BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN can_update BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN can_delete BOOLEAN NOT NULL DEFAULT FALSE;

-- 2. Remove the old split-per-operation functions. function_urls and
--    role_functions rows referencing them cascade-delete automatically
--    (both FKs are ON DELETE CASCADE, from V10/V12).
DELETE FROM functions WHERE function_code IN (
    'INVENTORY_ITEMS_MANAGE', 'INVENTORY_ITEMS_DELETE',
    'INVENTORY_CATEGORIES_MANAGE', 'INVENTORY_CATEGORIES_DELETE',
    'SUPPLIERS_MANAGE', 'SUPPLIERS_DELETE',
    'STOCK_OPERATIONS', 'ORDER_VIEW', 'ORDER_UPDATE'
);

-- 3. One function per module.
INSERT INTO functions (function_code, function_name, description) VALUES
    ('INVENTORY_ITEMS', 'Inventory Items', 'Raw material items'),
    ('INVENTORY_CATEGORIES', 'Inventory Categories', 'Inventory categories'),
    ('SUPPLIERS', 'Suppliers', 'Supplier directory'),
    ('STOCK_OPERATIONS', 'Stock Operations', 'Record and view stock movements'),
    ('ORDER_MANAGEMENT', 'Order Management', 'View and edit customer orders');

-- 4. Re-map URLs to the consolidated functions. Each row is flagged with the
--    ONE operation it represents - RbacAuthorizationService uses that flag
--    to pick which column to check on the caller's role_functions grant.
INSERT INTO function_urls (function_id, url, http_method, can_create, can_read, can_update, can_delete)
SELECT id, '/api/v1/inventory/items', 'POST', TRUE, FALSE, FALSE, FALSE FROM functions WHERE function_code = 'INVENTORY_ITEMS'
UNION ALL SELECT id, '/api/v1/inventory/items', 'GET', FALSE, TRUE, FALSE, FALSE FROM functions WHERE function_code = 'INVENTORY_ITEMS'
UNION ALL SELECT id, '/api/v1/inventory/items/*', 'GET', FALSE, TRUE, FALSE, FALSE FROM functions WHERE function_code = 'INVENTORY_ITEMS'
UNION ALL SELECT id, '/api/v1/inventory/items/*', 'PATCH', FALSE, FALSE, TRUE, FALSE FROM functions WHERE function_code = 'INVENTORY_ITEMS'
UNION ALL SELECT id, '/api/v1/inventory/items/*', 'DELETE', FALSE, FALSE, FALSE, TRUE FROM functions WHERE function_code = 'INVENTORY_ITEMS'

UNION ALL SELECT id, '/api/v1/inventory/categories', 'POST', TRUE, FALSE, FALSE, FALSE FROM functions WHERE function_code = 'INVENTORY_CATEGORIES'
UNION ALL SELECT id, '/api/v1/inventory/categories', 'GET', FALSE, TRUE, FALSE, FALSE FROM functions WHERE function_code = 'INVENTORY_CATEGORIES'
UNION ALL SELECT id, '/api/v1/inventory/categories/*', 'GET', FALSE, TRUE, FALSE, FALSE FROM functions WHERE function_code = 'INVENTORY_CATEGORIES'
UNION ALL SELECT id, '/api/v1/inventory/categories/*', 'PATCH', FALSE, FALSE, TRUE, FALSE FROM functions WHERE function_code = 'INVENTORY_CATEGORIES'
UNION ALL SELECT id, '/api/v1/inventory/categories/*', 'DELETE', FALSE, FALSE, FALSE, TRUE FROM functions WHERE function_code = 'INVENTORY_CATEGORIES'

UNION ALL SELECT id, '/api/v1/inventory/suppliers', 'POST', TRUE, FALSE, FALSE, FALSE FROM functions WHERE function_code = 'SUPPLIERS'
UNION ALL SELECT id, '/api/v1/inventory/suppliers', 'GET', FALSE, TRUE, FALSE, FALSE FROM functions WHERE function_code = 'SUPPLIERS'
UNION ALL SELECT id, '/api/v1/inventory/suppliers/*', 'GET', FALSE, TRUE, FALSE, FALSE FROM functions WHERE function_code = 'SUPPLIERS'
UNION ALL SELECT id, '/api/v1/inventory/suppliers/*', 'PATCH', FALSE, FALSE, TRUE, FALSE FROM functions WHERE function_code = 'SUPPLIERS'
UNION ALL SELECT id, '/api/v1/inventory/suppliers/*', 'DELETE', FALSE, FALSE, FALSE, TRUE FROM functions WHERE function_code = 'SUPPLIERS'

UNION ALL SELECT id, '/api/v1/inventory/items/*/stock', 'POST', TRUE, FALSE, FALSE, FALSE FROM functions WHERE function_code = 'STOCK_OPERATIONS'
UNION ALL SELECT id, '/api/v1/inventory/items/*/consume', 'POST', TRUE, FALSE, FALSE, FALSE FROM functions WHERE function_code = 'STOCK_OPERATIONS'
UNION ALL SELECT id, '/api/v1/inventory/items/*/wastage', 'POST', TRUE, FALSE, FALSE, FALSE FROM functions WHERE function_code = 'STOCK_OPERATIONS'
UNION ALL SELECT id, '/api/v1/inventory/items/*/adjustment', 'POST', TRUE, FALSE, FALSE, FALSE FROM functions WHERE function_code = 'STOCK_OPERATIONS'
UNION ALL SELECT id, '/api/v1/inventory/items/*/return', 'POST', TRUE, FALSE, FALSE, FALSE FROM functions WHERE function_code = 'STOCK_OPERATIONS'
UNION ALL SELECT id, '/api/v1/inventory/items/*/movements', 'GET', FALSE, TRUE, FALSE, FALSE FROM functions WHERE function_code = 'STOCK_OPERATIONS'

UNION ALL SELECT id, '/api/v1/customer/orders', 'GET', FALSE, TRUE, FALSE, FALSE FROM functions WHERE function_code = 'ORDER_MANAGEMENT'
UNION ALL SELECT id, '/api/v1/customer/orders', 'PATCH', FALSE, FALSE, TRUE, FALSE FROM functions WHERE function_code = 'ORDER_MANAGEMENT';

-- 5. Role grants, now carrying explicit CRUD flags per (role, function).
-- ADMIN: full CRUD on every function.
INSERT INTO role_functions (role_id, function_id, can_create, can_read, can_update, can_delete)
SELECT r.id, f.id, TRUE, TRUE, TRUE, TRUE
FROM roles r CROSS JOIN functions f
WHERE r.role_code = 'ADMIN';

-- STAFF: create/read/update (no delete) on inventory modules + stock ops.
INSERT INTO role_functions (role_id, function_id, can_create, can_read, can_update, can_delete)
SELECT r.id, f.id, TRUE, TRUE, TRUE, FALSE
FROM roles r, functions f
WHERE r.role_code = 'STAFF'
  AND f.function_code IN ('INVENTORY_ITEMS', 'INVENTORY_CATEGORIES', 'SUPPLIERS', 'STOCK_OPERATIONS');

-- STAFF: read-only on order management (cannot edit a placed order).
INSERT INTO role_functions (role_id, function_id, can_create, can_read, can_update, can_delete)
SELECT r.id, f.id, FALSE, TRUE, FALSE, FALSE
FROM roles r, functions f
WHERE r.role_code = 'STAFF' AND f.function_code = 'ORDER_MANAGEMENT';

-- CUSTOMER: no grants - relies entirely on public/guest endpoints.
