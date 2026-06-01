-- Restrict admin dashboard analytics independently from other admin capabilities.
INSERT INTO permission (name, description)
VALUES ('READ_ADMIN_DASHBOARD', 'Read aggregated admin dashboard metrics')
ON CONFLICT (name) DO UPDATE
SET description = EXCLUDED.description;

INSERT INTO role_permission (role_name, permission_name)
VALUES
    ('BUSINESS_ADMIN', 'READ_ADMIN_DASHBOARD'),
    ('SYSTEM_ADMIN', 'READ_ADMIN_DASHBOARD')
ON CONFLICT DO NOTHING;
