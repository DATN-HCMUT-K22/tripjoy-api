-- ============================================================
-- V3 - Seed sample users
-- Seeds deterministic test users user1..user5 with USER role.
-- ============================================================

INSERT INTO users (
    id,
    created_at,
    updated_at,
    username,
    password,
    email,
    is_email_verified,
    full_name,
    is_locked,
    is_deleted
)
SELECT
    seed.id,
    NOW(),
    NOW(),
    seed.username,
    '$2a$10$ErFsL9I5oUas/EDbrlZ7cOWoxYwDEiQpedAxiLEEav75a1X1l8N6S',
    seed.email,
    TRUE,
    seed.full_name,
    FALSE,
    FALSE
FROM (
    VALUES
        ('00000000-0000-0000-0000-000000000011'::UUID, 'user1', 'user1@tripjoy.vn', 'User 1'),
        ('00000000-0000-0000-0000-000000000012'::UUID, 'user2', 'user2@tripjoy.vn', 'User 2'),
        ('00000000-0000-0000-0000-000000000013'::UUID, 'user3', 'user3@tripjoy.vn', 'User 3'),
        ('00000000-0000-0000-0000-000000000014'::UUID, 'user4', 'user4@tripjoy.vn', 'User 4'),
        ('00000000-0000-0000-0000-000000000015'::UUID, 'user5', 'user5@tripjoy.vn', 'User 5')
) AS seed(id, username, email, full_name)
WHERE NOT EXISTS (
    SELECT 1
    FROM users existing_user
    WHERE existing_user.id = seed.id
       OR existing_user.username = seed.username
       OR existing_user.email = seed.email
);

INSERT INTO user_role (users_id, role_name)
SELECT seed.id, 'USER'
FROM (
    VALUES
        ('00000000-0000-0000-0000-000000000011'::UUID),
        ('00000000-0000-0000-0000-000000000012'::UUID),
        ('00000000-0000-0000-0000-000000000013'::UUID),
        ('00000000-0000-0000-0000-000000000014'::UUID),
        ('00000000-0000-0000-0000-000000000015'::UUID)
) AS seed(id)
JOIN users u ON u.id = seed.id
JOIN role r ON r.name = 'USER'
ON CONFLICT DO NOTHING;
