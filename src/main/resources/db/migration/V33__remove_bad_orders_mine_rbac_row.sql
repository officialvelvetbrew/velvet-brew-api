-- A function_urls row mapping GET /api/v1/customer/orders/mine to
-- ORDER_MANAGEMENT was inserted directly into the database (not through any
-- migration - id=1, predating the RBAC schema's own migrations). This gates
-- a CUSTOMER self-service endpoint (a caller's own orders) behind a
-- staff/admin-only function that CUSTOMER holds zero grants for by design
-- (see V25's "CUSTOMER: no grants" comment), so every customer was denied
-- with a 403 on their own order history despite SecurityConfig already
-- correctly allowing any authenticated caller on this route.
--
-- ORDER_MANAGEMENT is meant to gate the admin/staff "view all orders" list
-- (GET /api/v1/customer/orders, no suffix) - that row is untouched.
DELETE FROM function_urls
WHERE url = '/api/v1/customer/orders/mine'
  AND http_method = 'GET';
