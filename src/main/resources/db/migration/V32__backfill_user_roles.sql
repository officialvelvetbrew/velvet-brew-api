-- V24 introduced the fine-grained RBAC check (RbacAuthorizationService),
-- which authorizes off user_roles rather than the legacy users.role column.
-- Every user created before that rollout has a users.role value but no
-- matching user_roles row, so RbacAuthorizationService.isAllowed() sees an
-- empty role set and denies them (403) on any URL matched by function_urls,
-- even though SecurityConfig's coarser hasRole/hasAnyRole check still
-- passes. Backfill user_roles from the existing users.role for everyone.
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.role_code = u.role::text
ON CONFLICT (user_id, role_id) DO NOTHING;
