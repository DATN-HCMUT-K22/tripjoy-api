-- ============================================================
-- V2 - Seed RBAC baseline only
-- Seeds permissions, roles, and role-permission mappings.
-- User records and demo/business data are intentionally not seeded.
-- ============================================================

INSERT INTO permission (name, description) VALUES
    ('READ_USER', 'Read user profiles'),
    ('WRITE_USER', 'Create and update users'),
    ('DELETE_USER', 'Delete users'),
    ('MANAGE_ROLES', 'Assign and revoke roles'),
    ('READ_LOCATION', 'Read locations'),
    ('WRITE_LOCATION', 'Create and update locations'),
    ('DELETE_LOCATION', 'Delete locations'),
    ('READ_POST', 'Read posts'),
    ('WRITE_POST', 'Create and update posts'),
    ('DELETE_POST', 'Delete posts'),
    ('READ_GROUP', 'Read groups'),
    ('WRITE_GROUP', 'Create and update groups'),
    ('DELETE_GROUP', 'Delete groups'),
    ('READ_REPORT', 'Read user reports'),
    ('HANDLE_REPORT', 'Handle user reports'),
    ('READ_FEEDBACK', 'Read user feedback'),
    ('RESPOND_FEEDBACK', 'Respond to user feedback'),
    ('MODERATE_USER', 'Perform moderation actions')
ON CONFLICT (name) DO UPDATE
SET description = EXCLUDED.description;

INSERT INTO role (name, description) VALUES
    ('USER', 'Standard registered user'),
    ('BUSINESS_ADMIN', 'Business administration and content moderation access'),
    ('SYSTEM_ADMIN', 'Full system administration access')
ON CONFLICT (name) DO UPDATE
SET description = EXCLUDED.description;

INSERT INTO role_permission (role_name, permission_name) VALUES
    ('USER', 'READ_USER'),
    ('USER', 'READ_LOCATION'),
    ('USER', 'READ_POST'),
    ('USER', 'WRITE_POST'),
    ('USER', 'READ_GROUP'),
    ('USER', 'WRITE_GROUP'),

    ('BUSINESS_ADMIN', 'READ_USER'),
    ('BUSINESS_ADMIN', 'READ_LOCATION'),
    ('BUSINESS_ADMIN', 'READ_POST'),
    ('BUSINESS_ADMIN', 'WRITE_POST'),
    ('BUSINESS_ADMIN', 'DELETE_POST'),
    ('BUSINESS_ADMIN', 'READ_GROUP'),
    ('BUSINESS_ADMIN', 'READ_REPORT'),
    ('BUSINESS_ADMIN', 'HANDLE_REPORT'),
    ('BUSINESS_ADMIN', 'READ_FEEDBACK'),
    ('BUSINESS_ADMIN', 'RESPOND_FEEDBACK'),
    ('BUSINESS_ADMIN', 'MODERATE_USER')
ON CONFLICT DO NOTHING;

INSERT INTO role_permission (role_name, permission_name)
SELECT 'SYSTEM_ADMIN', name
FROM permission
ON CONFLICT DO NOTHING;
