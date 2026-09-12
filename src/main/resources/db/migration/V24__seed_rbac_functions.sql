-- Canonical roles matching the existing Role enum (ADMIN/STAFF/CUSTOMER), so
-- user_roles can reference them. A user's legacy users.role column keeps
-- driving the coarse SecurityConfig rules (hasRole/hasAnyRole); user_roles
-- additionally drives the fine-grained function_urls permission check in
-- RbacAuthorizationService. A user meant to be an admin/staff member needs
-- BOTH: users.role updated AND a matching row inserted into user_roles.
INSERT INTO roles (role_code, role_name, description) VALUES
    ('ADMIN', 'Administrator', 'Full access to every function'),
    ('STAFF', 'Staff', 'Day-to-day cafe operations'),
    ('CUSTOMER', 'Customer', 'Customer-facing access only, no back-office functions');

-- Functions. Split into *_MANAGE (create/read/update) and *_DELETE per module
-- so a role can be granted the ability to manage a module without also being
-- able to delete its records - role_functions only grants whole functions,
-- it has no CRUD granularity of its own, so that split is what makes
-- per-operation differences between roles possible with this schema.
INSERT INTO functions (function_code, function_name, description) VALUES
    ('INVENTORY_ITEMS_MANAGE', 'Manage Inventory Items', 'Create, view, and update inventory items'),
    ('INVENTORY_ITEMS_DELETE', 'Delete Inventory Items', 'Delete inventory items'),
    ('INVENTORY_CATEGORIES_MANAGE', 'Manage Inventory Categories', 'Create, view, and update inventory categories'),
    ('INVENTORY_CATEGORIES_DELETE', 'Delete Inventory Categories', 'Delete inventory categories'),
    ('SUPPLIERS_MANAGE', 'Manage Suppliers', 'Create, view, and update suppliers'),
    ('SUPPLIERS_DELETE', 'Delete Suppliers', 'Delete suppliers'),
    ('STOCK_OPERATIONS', 'Stock Operations', 'Add, consume, adjust, return stock and view movement history'),
    ('ORDER_VIEW', 'View All Orders', 'List every order in the system'),
    ('ORDER_UPDATE', 'Update Orders', 'Edit an existing order after it was placed');

-- URL -> function mappings. Patterns use Spring's AntPathMatcher syntax
-- (matched in RbacAuthorizationService): "*" = exactly one path segment,
-- "**" = any number of segments.
INSERT INTO function_urls (function_id, url, http_method, can_create, can_read, can_update, can_delete)
SELECT id, '/api/v1/inventory/items', 'POST', TRUE, FALSE, FALSE, FALSE FROM functions WHERE function_code = 'INVENTORY_ITEMS_MANAGE'
UNION ALL SELECT id, '/api/v1/inventory/items', 'GET', FALSE, TRUE, FALSE, FALSE FROM functions WHERE function_code = 'INVENTORY_ITEMS_MANAGE'
UNION ALL SELECT id, '/api/v1/inventory/items/*', 'GET', FALSE, TRUE, FALSE, FALSE FROM functions WHERE function_code = 'INVENTORY_ITEMS_MANAGE'
UNION ALL SELECT id, '/api/v1/inventory/items/*', 'PATCH', FALSE, FALSE, TRUE, FALSE FROM functions WHERE function_code = 'INVENTORY_ITEMS_MANAGE'

UNION ALL SELECT id, '/api/v1/inventory/items/*', 'DELETE', FALSE, FALSE, FALSE, TRUE FROM functions WHERE function_code = 'INVENTORY_ITEMS_DELETE'

UNION ALL SELECT id, '/api/v1/inventory/categories', 'POST', TRUE, FALSE, FALSE, FALSE FROM functions WHERE function_code = 'INVENTORY_CATEGORIES_MANAGE'
UNION ALL SELECT id, '/api/v1/inventory/categories', 'GET', FALSE, TRUE, FALSE, FALSE FROM functions WHERE function_code = 'INVENTORY_CATEGORIES_MANAGE'
UNION ALL SELECT id, '/api/v1/inventory/categories/*', 'GET', FALSE, TRUE, FALSE, FALSE FROM functions WHERE function_code = 'INVENTORY_CATEGORIES_MANAGE'
UNION ALL SELECT id, '/api/v1/inventory/categories/*', 'PATCH', FALSE, FALSE, TRUE, FALSE FROM functions WHERE function_code = 'INVENTORY_CATEGORIES_MANAGE'

UNION ALL SELECT id, '/api/v1/inventory/categories/*', 'DELETE', FALSE, FALSE, FALSE, TRUE FROM functions WHERE function_code = 'INVENTORY_CATEGORIES_DELETE'

UNION ALL SELECT id, '/api/v1/inventory/suppliers', 'POST', TRUE, FALSE, FALSE, FALSE FROM functions WHERE function_code = 'SUPPLIERS_MANAGE'
UNION ALL SELECT id, '/api/v1/inventory/suppliers', 'GET', FALSE, TRUE, FALSE, FALSE FROM functions WHERE function_code = 'SUPPLIERS_MANAGE'
UNION ALL SELECT id, '/api/v1/inventory/suppliers/*', 'GET', FALSE, TRUE, FALSE, FALSE FROM functions WHERE function_code = 'SUPPLIERS_MANAGE'
UNION ALL SELECT id, '/api/v1/inventory/suppliers/*', 'PATCH', FALSE, FALSE, TRUE, FALSE FROM functions WHERE function_code = 'SUPPLIERS_MANAGE'

UNION ALL SELECT id, '/api/v1/inventory/suppliers/*', 'DELETE', FALSE, FALSE, FALSE, TRUE FROM functions WHERE function_code = 'SUPPLIERS_DELETE'

UNION ALL SELECT id, '/api/v1/inventory/items/*/stock', 'POST', TRUE, FALSE, FALSE, FALSE FROM functions WHERE function_code = 'STOCK_OPERATIONS'
UNION ALL SELECT id, '/api/v1/inventory/items/*/consume', 'POST', TRUE, FALSE, FALSE, FALSE FROM functions WHERE function_code = 'STOCK_OPERATIONS'
UNION ALL SELECT id, '/api/v1/inventory/items/*/wastage', 'POST', TRUE, FALSE, FALSE, FALSE FROM functions WHERE function_code = 'STOCK_OPERATIONS'
UNION ALL SELECT id, '/api/v1/inventory/items/*/adjustment', 'POST', TRUE, FALSE, FALSE, FALSE FROM functions WHERE function_code = 'STOCK_OPERATIONS'
UNION ALL SELECT id, '/api/v1/inventory/items/*/return', 'POST', TRUE, FALSE, FALSE, FALSE FROM functions WHERE function_code = 'STOCK_OPERATIONS'
UNION ALL SELECT id, '/api/v1/inventory/items/*/movements', 'GET', FALSE, TRUE, FALSE, FALSE FROM functions WHERE function_code = 'STOCK_OPERATIONS'

UNION ALL SELECT id, '/api/v1/customer/orders', 'GET', FALSE, TRUE, FALSE, FALSE FROM functions WHERE function_code = 'ORDER_VIEW'

UNION ALL SELECT id, '/api/v1/customer/orders', 'PATCH', FALSE, FALSE, TRUE, FALSE FROM functions WHERE function_code = 'ORDER_UPDATE';

-- Role -> function grants.
-- ADMIN: every function.
INSERT INTO role_functions (role_id, function_id)
SELECT r.id, f.id FROM roles r CROSS JOIN functions f WHERE r.role_code = 'ADMIN';

-- STAFF: can manage inventory/categories/suppliers and run stock operations,
-- and can view all orders - but cannot delete inventory/category/supplier
-- records and cannot edit an order after it's placed. Adjust to taste by
-- editing role_functions directly; no code change needed.
INSERT INTO role_functions (role_id, function_id)
SELECT r.id, f.id FROM roles r, functions f
WHERE r.role_code = 'STAFF'
  AND f.function_code IN (
      'INVENTORY_ITEMS_MANAGE',
      'INVENTORY_CATEGORIES_MANAGE',
      'SUPPLIERS_MANAGE',
      'STOCK_OPERATIONS',
      'ORDER_VIEW'
  );

-- CUSTOMER: no back-office functions - relies entirely on the public/guest
-- endpoints already open in SecurityConfig.
