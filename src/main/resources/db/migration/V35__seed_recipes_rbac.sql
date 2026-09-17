-- Seeds RBAC for the new recipe management module, following the
-- one-function-per-module / CRUD-flag-per-grant pattern introduced in V25.

INSERT INTO functions (function_code, function_name, description) VALUES
    ('RECIPES', 'Recipes', 'Menu item recipes and their ingredient quantities');

INSERT INTO function_urls (function_id, url, http_method, can_create, can_read, can_update, can_delete)
SELECT id, '/api/v1/inventory/recipes', 'POST', TRUE, FALSE, FALSE, FALSE FROM functions WHERE function_code = 'RECIPES'
UNION ALL SELECT id, '/api/v1/inventory/recipes', 'GET', FALSE, TRUE, FALSE, FALSE FROM functions WHERE function_code = 'RECIPES'
UNION ALL SELECT id, '/api/v1/inventory/recipes/*', 'GET', FALSE, TRUE, FALSE, FALSE FROM functions WHERE function_code = 'RECIPES'
UNION ALL SELECT id, '/api/v1/inventory/recipes/*', 'PATCH', FALSE, FALSE, TRUE, FALSE FROM functions WHERE function_code = 'RECIPES'
UNION ALL SELECT id, '/api/v1/inventory/recipes/*', 'DELETE', FALSE, FALSE, FALSE, TRUE FROM functions WHERE function_code = 'RECIPES'
UNION ALL SELECT id, '/api/v1/inventory/recipes/menu-item/*', 'GET', FALSE, TRUE, FALSE, FALSE FROM functions WHERE function_code = 'RECIPES';

-- ADMIN: full CRUD.
INSERT INTO role_functions (role_id, function_id, can_create, can_read, can_update, can_delete)
SELECT r.id, f.id, TRUE, TRUE, TRUE, TRUE
FROM roles r, functions f
WHERE r.role_code = 'ADMIN' AND f.function_code = 'RECIPES';

-- STAFF: create/read/update, no delete - matches the other inventory modules.
INSERT INTO role_functions (role_id, function_id, can_create, can_read, can_update, can_delete)
SELECT r.id, f.id, TRUE, TRUE, TRUE, FALSE
FROM roles r, functions f
WHERE r.role_code = 'STAFF' AND f.function_code = 'RECIPES';
