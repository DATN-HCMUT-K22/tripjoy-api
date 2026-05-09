-- ============================================================
-- V3 — Seed Test Data (Dev & Staging Only)
-- ============================================================
-- ⚠️  DO NOT RUN ON PRODUCTION
-- Password for all users: StrongP@ss123
-- BCrypt hash ($2a$10$, cost=10) pre-computed via BCryptPasswordEncoder
-- Hash: $2a$10$ErFsL9I5oUas/EDbrlZ7cOWoxYwDEiQpedAxiLEEav75a1X1l8N6S
--
-- If this hash doesn't match, re-generate with:
--   new BCryptPasswordEncoder().encode("StrongP@ss123")
-- and replace the hash in this file.
-- ============================================================

-- ============================================================
-- 1. PERMISSIONS
-- ============================================================
INSERT INTO permission (name, description) VALUES
    ('READ_USER',        'Read user profiles'),
    ('WRITE_USER',       'Create and update users'),
    ('DELETE_USER',      'Delete users'),
    ('MANAGE_ROLES',     'Assign and revoke roles'),
    ('READ_LOCATION',    'Read locations'),
    ('WRITE_LOCATION',   'Create and update locations'),
    ('DELETE_LOCATION',  'Delete locations'),
    ('READ_POST',        'Read posts'),
    ('WRITE_POST',       'Create and update posts'),
    ('DELETE_POST',      'Delete posts'),
    ('READ_GROUP',       'Read groups'),
    ('WRITE_GROUP',      'Create and update groups'),
    ('DELETE_GROUP',     'Delete groups')
ON CONFLICT (name) DO NOTHING;

-- ============================================================
-- 2. ROLES
-- ============================================================
INSERT INTO role (name, description) VALUES
    ('ADMIN',     'Full system access'),
    ('USER',      'Standard registered user'),
    ('MODERATOR', 'Content moderation access')
ON CONFLICT (name) DO NOTHING;

-- ============================================================
-- 3. ROLE PERMISSIONS
-- ============================================================
-- ADMIN gets everything
INSERT INTO role_permission (role_name, permission_name)
SELECT 'ADMIN', name FROM permission
ON CONFLICT DO NOTHING;

-- USER gets read + write (no delete/manage)
INSERT INTO role_permission (role_name, permission_name) VALUES
    ('USER', 'READ_USER'),
    ('USER', 'READ_LOCATION'),
    ('USER', 'READ_POST'),
    ('USER', 'WRITE_POST'),
    ('USER', 'READ_GROUP'),
    ('USER', 'WRITE_GROUP')
ON CONFLICT DO NOTHING;

-- MODERATOR can delete posts/comments
INSERT INTO role_permission (role_name, permission_name) VALUES
    ('MODERATOR', 'READ_USER'),
    ('MODERATOR', 'READ_LOCATION'),
    ('MODERATOR', 'READ_POST'),
    ('MODERATOR', 'WRITE_POST'),
    ('MODERATOR', 'DELETE_POST'),
    ('MODERATOR', 'READ_GROUP')
ON CONFLICT DO NOTHING;

-- ============================================================
-- 4. USERS  (password = StrongP@ss123)
-- BCrypt hash: $2a$10$ErFsL9I5oUas/EDbrlZ7cOWoxYwDEiQpedAxiLEEav75a1X1l8N6S
-- ============================================================
INSERT INTO users (id, created_at, updated_at, username, password, email,
                   is_email_verified, full_name, is_locked, is_deleted)
VALUES
    -- admin
    ('00000000-0000-0000-0000-000000000001', NOW(), NOW(),
     'admin',  '$2a$10$ErFsL9I5oUas/EDbrlZ7cOWoxYwDEiQpedAxiLEEav75a1X1l8N6S',
     'admin@tripjoy.vn', TRUE, 'TripJoy Admin', FALSE, FALSE),

    -- user1 — Dinh Duc
    ('00000000-0000-0000-0000-000000000011', NOW(), NOW(),
     'user1',  '$2a$10$ErFsL9I5oUas/EDbrlZ7cOWoxYwDEiQpedAxiLEEav75a1X1l8N6S',
     'user1@tripjoy.vn', TRUE, 'Dinh Duc', FALSE, FALSE),

    -- user2 — Ngoc Ha
    ('00000000-0000-0000-0000-000000000012', NOW(), NOW(),
     'user2',  '$2a$10$ErFsL9I5oUas/EDbrlZ7cOWoxYwDEiQpedAxiLEEav75a1X1l8N6S',
     'user2@tripjoy.vn', TRUE, 'Ngoc Ha', FALSE, FALSE),

    -- user3 — Huy Duc
    ('00000000-0000-0000-0000-000000000013', NOW(), NOW(),
     'user3',  '$2a$10$ErFsL9I5oUas/EDbrlZ7cOWoxYwDEiQpedAxiLEEav75a1X1l8N6S',
     'user3@tripjoy.vn', TRUE, 'Huy Duc', FALSE, FALSE),

    -- user4 — Ngoc Chau
    ('00000000-0000-0000-0000-000000000014', NOW(), NOW(),
     'user4',  '$2a$10$ErFsL9I5oUas/EDbrlZ7cOWoxYwDEiQpedAxiLEEav75a1X1l8N6S',
     'user4@tripjoy.vn', TRUE, 'Ngoc Chau', FALSE, FALSE),

    -- user5 — Minh Tuan
    ('00000000-0000-0000-0000-000000000015', NOW(), NOW(),
     'user5',  '$2a$10$ErFsL9I5oUas/EDbrlZ7cOWoxYwDEiQpedAxiLEEav75a1X1l8N6S',
     'user5@tripjoy.vn', TRUE, 'Minh Tuan', FALSE, FALSE),

    -- user6 — Thu Thao
    ('00000000-0000-0000-0000-000000000016', NOW(), NOW(),
     'user6',  '$2a$10$ErFsL9I5oUas/EDbrlZ7cOWoxYwDEiQpedAxiLEEav75a1X1l8N6S',
     'user6@tripjoy.vn', TRUE, 'Thu Thao', FALSE, FALSE),

    -- user7 — Quoc Bao
    ('00000000-0000-0000-0000-000000000017', NOW(), NOW(),
     'user7',  '$2a$10$ErFsL9I5oUas/EDbrlZ7cOWoxYwDEiQpedAxiLEEav75a1X1l8N6S',
     'user7@tripjoy.vn', TRUE, 'Quoc Bao', FALSE, FALSE),

    -- user8 — Phuong Linh
    ('00000000-0000-0000-0000-000000000018', NOW(), NOW(),
     'user8',  '$2a$10$ErFsL9I5oUas/EDbrlZ7cOWoxYwDEiQpedAxiLEEav75a1X1l8N6S',
     'user8@tripjoy.vn', TRUE, 'Phuong Linh', FALSE, FALSE),

    -- user9 — Hoang Nam
    ('00000000-0000-0000-0000-000000000019', NOW(), NOW(),
     'user9',  '$2a$10$ErFsL9I5oUas/EDbrlZ7cOWoxYwDEiQpedAxiLEEav75a1X1l8N6S',
     'user9@tripjoy.vn', TRUE, 'Hoang Nam', FALSE, FALSE),

    -- user10 — Kim Anh
    ('00000000-0000-0000-0000-000000000020', NOW(), NOW(),
     'user10', '$2a$10$ErFsL9I5oUas/EDbrlZ7cOWoxYwDEiQpedAxiLEEav75a1X1l8N6S',
     'user10@tripjoy.vn', TRUE, 'Kim Anh', FALSE, FALSE)

ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- 5. USER ROLES
-- ============================================================
INSERT INTO user_role (users_id, role_name) VALUES
    ('00000000-0000-0000-0000-000000000001', 'ADMIN'),
    ('00000000-0000-0000-0000-000000000011', 'USER'),
    ('00000000-0000-0000-0000-000000000012', 'USER'),
    ('00000000-0000-0000-0000-000000000013', 'USER'),
    ('00000000-0000-0000-0000-000000000014', 'USER'),
    ('00000000-0000-0000-0000-000000000015', 'USER'),
    ('00000000-0000-0000-0000-000000000016', 'USER'),
    ('00000000-0000-0000-0000-000000000017', 'MODERATOR'),
    ('00000000-0000-0000-0000-000000000018', 'USER'),
    ('00000000-0000-0000-0000-000000000019', 'USER'),
    ('00000000-0000-0000-0000-000000000020', 'USER')
ON CONFLICT DO NOTHING;

-- ============================================================
-- 6. THEMES
-- ============================================================
INSERT INTO theme (id, created_at, updated_at, name) VALUES
    ('10000000-0000-0000-0000-000000000001', NOW(), NOW(), 'Biển'),
    ('10000000-0000-0000-0000-000000000002', NOW(), NOW(), 'Núi'),
    ('10000000-0000-0000-0000-000000000003', NOW(), NOW(), 'Văn hóa'),
    ('10000000-0000-0000-0000-000000000004', NOW(), NOW(), 'Ẩm thực'),
    ('10000000-0000-0000-0000-000000000005', NOW(), NOW(), 'Phượt'),
    ('10000000-0000-0000-0000-000000000006', NOW(), NOW(), 'Nghỉ dưỡng'),
    ('10000000-0000-0000-0000-000000000007', NOW(), NOW(), 'Lịch sử'),
    ('10000000-0000-0000-0000-000000000008', NOW(), NOW(), 'Gia đình')
ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- 7. GROUPS
-- ============================================================
INSERT INTO groups (id, created_at, created_by, updated_at, updated_by,
                    name, description, chatbot_count, theme_color, is_pro, is_deleted)
VALUES
    ('a0000000-0000-0000-0000-000000000001',
     NOW(), '00000000-0000-0000-0000-000000000011', NOW(), '00000000-0000-0000-0000-000000000011',
     'Trip Đà Lạt 2026', 'Nhóm đi Đà Lạt tháng 4', 0, '#FF6B6B', false, false),

    ('a0000000-0000-0000-0000-000000000002',
     NOW(), '00000000-0000-0000-0000-000000000012', NOW(), '00000000-0000-0000-0000-000000000012',
     'Phú Quốc Summer', 'Du lịch Phú Quốc hè 2026', 0, '#4ECDC4', true, false),

    ('a0000000-0000-0000-0000-000000000003',
     NOW(), '00000000-0000-0000-0000-000000000013', NOW(), '00000000-0000-0000-0000-000000000013',
     'Sapa Trekking', 'Leo Fansipan cùng nhóm', 0, '#45B7D1', false, false)

ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- 8. GROUP MEMBERS
-- ============================================================
INSERT INTO group_member (id, created_at, updated_at, group_id, user_id, role, is_deleted)
VALUES
    -- Group 1 "Đà Lạt": user1=LEADER, user2=MEMBER, user3=MEMBER, user4=CO_LEADER
    ('b0000001-0000-0000-0000-000000000001', NOW(), NOW(), 'a0000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000011', 'LEADER',     false),
    ('b0000001-0000-0000-0000-000000000002', NOW(), NOW(), 'a0000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000012', 'MEMBER',     false),
    ('b0000001-0000-0000-0000-000000000003', NOW(), NOW(), 'a0000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000013', 'MEMBER',     false),
    ('b0000001-0000-0000-0000-000000000004', NOW(), NOW(), 'a0000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000014', 'CO_LEADER',  false),

    -- Group 2 "Phú Quốc": user2=LEADER, user1=CO_LEADER, user5=MEMBER, user6=MEMBER
    ('b0000002-0000-0000-0000-000000000001', NOW(), NOW(), 'a0000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000012', 'LEADER',     false),
    ('b0000002-0000-0000-0000-000000000002', NOW(), NOW(), 'a0000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000011', 'CO_LEADER',  false),
    ('b0000002-0000-0000-0000-000000000003', NOW(), NOW(), 'a0000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000015', 'MEMBER',     false),
    ('b0000002-0000-0000-0000-000000000004', NOW(), NOW(), 'a0000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000016', 'MEMBER',     false),

    -- Group 3 "Sapa": user3=LEADER, user7=MEMBER, user8=MEMBER, user9=MEMBER
    ('b0000003-0000-0000-0000-000000000001', NOW(), NOW(), 'a0000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000013', 'LEADER',     false),
    ('b0000003-0000-0000-0000-000000000002', NOW(), NOW(), 'a0000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000017', 'MEMBER',     false),
    ('b0000003-0000-0000-0000-000000000003', NOW(), NOW(), 'a0000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000018', 'MEMBER',     false),
    ('b0000003-0000-0000-0000-000000000004', NOW(), NOW(), 'a0000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000019', 'MEMBER',     false)

ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- 9. CONVERSATIONS
-- ============================================================
INSERT INTO conversation (id, created_at, created_by, updated_at, updated_by,
                          type, group_id, name, last_message_timestamp)
VALUES
    -- Group 1 chat
    ('c0000000-0000-0000-0000-000000000001',
     NOW(), '00000000-0000-0000-0000-000000000011', NOW(), '00000000-0000-0000-0000-000000000011',
     'GROUP', 'a0000000-0000-0000-0000-000000000001', 'Trip Đà Lạt 2026', NOW()),

    -- Group 2 chat
    ('c0000000-0000-0000-0000-000000000002',
     NOW(), '00000000-0000-0000-0000-000000000012', NOW(), '00000000-0000-0000-0000-000000000012',
     'GROUP', 'a0000000-0000-0000-0000-000000000002', 'Phú Quốc Summer', NOW()),

    -- Group 3 chat
    ('c0000000-0000-0000-0000-000000000003',
     NOW(), '00000000-0000-0000-0000-000000000013', NOW(), '00000000-0000-0000-0000-000000000013',
     'GROUP', 'a0000000-0000-0000-0000-000000000003', 'Sapa Trekking', NOW()),

    -- Direct: user1 ↔ user4
    ('c0000000-0000-0000-0000-000000000004',
     NOW(), '00000000-0000-0000-0000-000000000011', NOW(), '00000000-0000-0000-0000-000000000011',
     'DIRECT', NULL, NULL, NOW())

ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- 10. CONVERSATION MEMBERS
-- ============================================================
INSERT INTO conversation_member (id, created_at, updated_at, conversation_id, user_id, unread_count, is_muted, is_pinned)
VALUES
    -- Conv 1 (Đà Lạt group: user1, user2, user3, user4)
    ('d0000001-0001-0000-0000-000000000001', NOW(), NOW(), 'c0000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000011', 0, false, false),
    ('d0000001-0002-0000-0000-000000000001', NOW(), NOW(), 'c0000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000012', 0, false, false),
    ('d0000001-0003-0000-0000-000000000001', NOW(), NOW(), 'c0000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000013', 0, false, false),
    ('d0000001-0004-0000-0000-000000000001', NOW(), NOW(), 'c0000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000014', 0, false, false),

    -- Conv 2 (Phú Quốc group: user2, user1, user5, user6)
    ('d0000002-0001-0000-0000-000000000001', NOW(), NOW(), 'c0000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000012', 0, false, false),
    ('d0000002-0002-0000-0000-000000000001', NOW(), NOW(), 'c0000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000011', 0, false, false),
    ('d0000002-0003-0000-0000-000000000001', NOW(), NOW(), 'c0000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000015', 0, false, false),
    ('d0000002-0004-0000-0000-000000000001', NOW(), NOW(), 'c0000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000016', 0, false, false),

    -- Conv 3 (Sapa group)
    ('d0000003-0001-0000-0000-000000000001', NOW(), NOW(), 'c0000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000013', 0, false, false),
    ('d0000003-0002-0000-0000-000000000001', NOW(), NOW(), 'c0000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000017', 0, false, false),
    ('d0000003-0003-0000-0000-000000000001', NOW(), NOW(), 'c0000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000018', 0, false, false),
    ('d0000003-0004-0000-0000-000000000001', NOW(), NOW(), 'c0000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000019', 0, false, false),

    -- Conv 4 (Direct user1 ↔ user4)
    ('d0000004-0001-0000-0000-000000000001', NOW(), NOW(), 'c0000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000011', 0, false, false),
    ('d0000004-0002-0000-0000-000000000001', NOW(), NOW(), 'c0000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000014', 0, false, false)

ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- 11. CHAT MESSAGES
-- ============================================================
INSERT INTO chat_message (id, created_at, created_by, updated_at, updated_by,
                          message_type, message_content, is_bot, status, is_pinned,
                          sender_id, conversation_id)
VALUES
    -- Conv 1 — Trip Đà Lạt
    ('e0000001-0001-0000-0000-000000000001', NOW() - INTERVAL '3 hours', '00000000-0000-0000-0000-000000000011', NOW() - INTERVAL '3 hours', '00000000-0000-0000-0000-000000000011',
     'TEXT', 'Chào mọi người! Mình tạo nhóm đi Đà Lạt nè 🎉', false, 'SENT', false, '00000000-0000-0000-0000-000000000011', 'c0000000-0000-0000-0000-000000000001'),

    ('e0000001-0002-0000-0000-000000000001', NOW() - INTERVAL '2 hours 50 min', '00000000-0000-0000-0000-000000000012', NOW() - INTERVAL '2 hours 50 min', '00000000-0000-0000-0000-000000000012',
     'TEXT', 'Oke nha! Đà Lạt mùa này đẹp lắm, hoa anh đào nở rồi 🌸', false, 'SENT', false, '00000000-0000-0000-0000-000000000012', 'c0000000-0000-0000-0000-000000000001'),

    ('e0000001-0003-0000-0000-000000000001', NOW() - INTERVAL '2 hours 40 min', '00000000-0000-0000-0000-000000000011', NOW() - INTERVAL '2 hours 40 min', '00000000-0000-0000-0000-000000000011',
     'TEXT', 'Dự kiến đi từ ngày 15 đến 18 tháng 4. Mọi người sắp xếp lịch nhé!', false, 'SENT', true, '00000000-0000-0000-0000-000000000011', 'c0000000-0000-0000-0000-000000000001'),

    ('e0000001-0004-0000-0000-000000000001', NOW() - INTERVAL '2 hours 30 min', '00000000-0000-0000-0000-000000000013', NOW() - INTERVAL '2 hours 30 min', '00000000-0000-0000-0000-000000000013',
     'TEXT', 'Homestay hay khách sạn vậy mọi người? Mình muốn view đẹp 📸', false, 'SENT', false, '00000000-0000-0000-0000-000000000013', 'c0000000-0000-0000-0000-000000000001'),

    ('e0000001-0005-0000-0000-000000000001', NOW() - INTERVAL '2 hours', '00000000-0000-0000-0000-000000000014', NOW() - INTERVAL '2 hours', '00000000-0000-0000-0000-000000000014',
     'TEXT', 'Mình biết quán bánh căn Đà Lạt ngon lắm, mọi người phải thử! 😋', false, 'SENT', false, '00000000-0000-0000-0000-000000000014', 'c0000000-0000-0000-0000-000000000001'),

    -- Conv 2 — Phú Quốc Summer
    ('e0000002-0001-0000-0000-000000000001', NOW() - INTERVAL '5 hours', '00000000-0000-0000-0000-000000000012', NOW() - INTERVAL '5 hours', '00000000-0000-0000-0000-000000000012',
     'TEXT', 'Chào team Phú Quốc! Hè này đi biển nha mọi người 🏖️', false, 'SENT', false, '00000000-0000-0000-0000-000000000012', 'c0000000-0000-0000-0000-000000000002'),

    ('e0000002-0002-0000-0000-000000000001', NOW() - INTERVAL '4 hours 45 min', '00000000-0000-0000-0000-000000000011', NOW() - INTERVAL '4 hours 45 min', '00000000-0000-0000-0000-000000000011',
     'TEXT', 'Tuyệt! Mình muốn đi snorkeling lặn biển 🤿', false, 'SENT', false, '00000000-0000-0000-0000-000000000011', 'c0000000-0000-0000-0000-000000000002'),

    ('e0000002-0003-0000-0000-000000000001', NOW() - INTERVAL '4 hours 30 min', '00000000-0000-0000-0000-000000000015', NOW() - INTERVAL '4 hours 30 min', '00000000-0000-0000-0000-000000000015',
     'TEXT', 'Book resort 4 sao ở Bãi Sao đi, 1.2 triệu/đêm chia 2 phòng', false, 'SENT', true, '00000000-0000-0000-0000-000000000015', 'c0000000-0000-0000-0000-000000000002'),

    ('e0000002-0004-0000-0000-000000000001', NOW() - INTERVAL '4 hours', '00000000-0000-0000-0000-000000000016', NOW() - INTERVAL '4 hours', '00000000-0000-0000-0000-000000000016',
     'TEXT', 'Mình sẽ mang kem chống nắng SPF 50+ cho cả nhóm 🧴', false, 'SENT', false, '00000000-0000-0000-0000-000000000016', 'c0000000-0000-0000-0000-000000000002'),

    -- Conv 3 — Sapa Trekking
    ('e0000003-0001-0000-0000-000000000001', NOW() - INTERVAL '1 day', '00000000-0000-0000-0000-000000000013', NOW() - INTERVAL '1 day', '00000000-0000-0000-0000-000000000013',
     'TEXT', 'Ai có thể lực tốt join team Fansipan không? Leo 5 ngày 🏔️', false, 'SENT', false, '00000000-0000-0000-0000-000000000013', 'c0000000-0000-0000-0000-000000000003'),

    ('e0000003-0002-0000-0000-000000000001', NOW() - INTERVAL '23 hours', '00000000-0000-0000-0000-000000000017', NOW() - INTERVAL '23 hours', '00000000-0000-0000-0000-000000000017',
     'TEXT', 'Mình đã leo Fansipan 2 lần rồi, để mình làm guide cho 🧭', false, 'SENT', false, '00000000-0000-0000-0000-000000000017', 'c0000000-0000-0000-0000-000000000003'),

    -- Conv 4 — Direct user1 ↔ user4
    ('e0000004-0001-0000-0000-000000000001', NOW() - INTERVAL '1 hour', '00000000-0000-0000-0000-000000000011', NOW() - INTERVAL '1 hour', '00000000-0000-0000-0000-000000000011',
     'TEXT', 'Chau ơi, bạn có muốn join trip Đà Lạt không? 😊', false, 'SENT', false, '00000000-0000-0000-0000-000000000011', 'c0000000-0000-0000-0000-000000000004'),

    ('e0000004-0002-0000-0000-000000000001', NOW() - INTERVAL '55 min', '00000000-0000-0000-0000-000000000014', NOW() - INTERVAL '55 min', '00000000-0000-0000-0000-000000000014',
     'TEXT', 'Oke luôn Duc ơi! Bao giờ đi vậy?', false, 'SENT', false, '00000000-0000-0000-0000-000000000014', 'c0000000-0000-0000-0000-000000000004')

ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- 12. HASHTAGS
-- ============================================================
INSERT INTO hashtag (id, created_at, updated_at, name) VALUES
    ('f0000000-0001-0000-0000-000000000001', NOW(), NOW(), 'dalat'),
    ('f0000000-0002-0000-0000-000000000001', NOW(), NOW(), 'phuquoc'),
    ('f0000000-0003-0000-0000-000000000001', NOW(), NOW(), 'sapa'),
    ('f0000000-0004-0000-0000-000000000001', NOW(), NOW(), 'trekking'),
    ('f0000000-0005-0000-0000-000000000001', NOW(), NOW(), 'phuot'),
    ('f0000000-0006-0000-0000-000000000001', NOW(), NOW(), 'review'),
    ('f0000000-0007-0000-0000-000000000001', NOW(), NOW(), 'resort'),
    ('f0000000-0008-0000-0000-000000000001', NOW(), NOW(), 'fansipan')
ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- 13. ITINERARIES
-- ============================================================
INSERT INTO itinerary (id, created_at, created_by, updated_at, updated_by,
                       name, description, start_date, end_date,
                       people_quantity, budget_estimate, status,
                       group_id, user_id, is_deleted)
VALUES
    -- Itinerary 1: Đà Lạt (group 1, owner user1)
    ('aa000001-0000-0000-0000-000000000001',
     NOW(), '00000000-0000-0000-0000-000000000011', NOW(), '00000000-0000-0000-0000-000000000011',
     'Đà Lạt Phượt Cùng Nhau', 'Chuyến đi tránh nóng tháng 4',
     NOW() + INTERVAL '30 days', NOW() + INTERVAL '34 days',
     4, 3000000.00, 'DRAFT',
     'a0000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000011', false),

    -- Itinerary 2: Phú Quốc (group 2, owner user2)
    ('aa000002-0000-0000-0000-000000000001',
     NOW(), '00000000-0000-0000-0000-000000000012', NOW(), '00000000-0000-0000-0000-000000000012',
     'Phú Quốc Resort Nghỉ Dưỡng', 'Chill hè bãi biển Phú Quốc',
     NOW() + INTERVAL '45 days', NOW() + INTERVAL '48 days',
     4, 8000000.00, 'DRAFT',
     'a0000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000012', false),

    -- Itinerary 3: Sapa (group 3, owner user3)
    ('aa000003-0000-0000-0000-000000000001',
     NOW(), '00000000-0000-0000-0000-000000000013', NOW(), '00000000-0000-0000-0000-000000000013',
     'Trekking Fansipan 5N4Đ', 'Leo núi và ngắm biển mây Sapa',
     NOW() + INTERVAL '60 days', NOW() + INTERVAL '65 days',
     4, 5000000.00, 'DRAFT',
     'a0000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000013', false),

    -- Itinerary 4: Personal trip user5 (Hội An)
    ('aa000004-0000-0000-0000-000000000001',
     NOW(), '00000000-0000-0000-0000-000000000015', NOW(), '00000000-0000-0000-0000-000000000015',
     'Hội An - Đà Nẵng Long Weekend', 'Khám phá phố cổ Hội An',
     NOW() + INTERVAL '14 days', NOW() + INTERVAL '17 days',
     2, 4000000.00, 'DRAFT',
     NULL, '00000000-0000-0000-0000-000000000015', false)

ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- 14. ITINERARY THEMES
-- ============================================================
INSERT INTO itinerary_theme_mapping (itinerary_id, theme_id) VALUES
    ('aa000001-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000002'), -- Đà Lạt + Núi
    ('aa000001-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000005'), -- Đà Lạt + Phượt
    ('aa000002-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001'), -- Phú Quốc + Biển
    ('aa000002-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000006'), -- Phú Quốc + Nghỉ dưỡng
    ('aa000003-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000002'), -- Sapa + Núi
    ('aa000003-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000005'), -- Sapa + Phượt
    ('aa000004-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000003'), -- Hội An + Văn hóa
    ('aa000004-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000004')  -- Hội An + Ẩm thực
ON CONFLICT DO NOTHING;

-- ============================================================
-- 15. POSTS
-- ============================================================
INSERT INTO post (id, created_at, created_by, updated_at, updated_by,
                  content, share_quantity, itinerary_id, creator_id, is_deleted)
VALUES
    ('bb000001-0000-0000-0000-000000000001',
     NOW() - INTERVAL '3 days', '00000000-0000-0000-0000-000000000011', NOW(), '00000000-0000-0000-0000-000000000011',
     'Mới về từ Đà Lạt! Sương mù siêu lãng mạn, cafe ngon, giá rẻ bèo. Ai cần kinh nghiệm thì inbox mình nhé 🌿',
     12, 'aa000001-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000011', false),

    ('bb000002-0000-0000-0000-000000000001',
     NOW() - INTERVAL '2 days', '00000000-0000-0000-0000-000000000012', NOW(), '00000000-0000-0000-0000-000000000012',
     'Review Phú Quốc: bãi biển xanh ngát, hải sản chợ đêm tươi sống tuyệt vời. Rất đáng tiền! 🏖️',
     45, 'aa000002-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000012', false),

    ('bb000003-0000-0000-0000-000000000001',
     NOW() - INTERVAL '1 day', '00000000-0000-0000-0000-000000000013', NOW(), '00000000-0000-0000-0000-000000000013',
     'Tìm đồng đội đi Trekking Fansipan tháng tới! 5 ngày leo hộc bơ, ngắm biển mây cực đẹp. Chi phí chia đều 🏔️',
     8, 'aa000003-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000013', false),

    ('bb000004-0000-0000-0000-000000000001',
     NOW() - INTERVAL '12 hours', '00000000-0000-0000-0000-000000000015', NOW(), '00000000-0000-0000-0000-000000000015',
     'Hội An mùa này đẹp lắm cả nhà ơi! Đèn lồng rực rỡ, bánh mì Phượng số 1 Việt Nam 🏮',
     22, NULL, '00000000-0000-0000-0000-000000000015', false)

ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- 16. POST HASHTAGS
-- ============================================================
INSERT INTO post_hashtag_mapping (post_id, hashtag_id) VALUES
    ('bb000001-0000-0000-0000-000000000001', 'f0000000-0001-0000-0000-000000000001'), -- dalat
    ('bb000001-0000-0000-0000-000000000001', 'f0000000-0005-0000-0000-000000000001'), -- phuot
    ('bb000001-0000-0000-0000-000000000001', 'f0000000-0006-0000-0000-000000000001'), -- review
    ('bb000002-0000-0000-0000-000000000001', 'f0000000-0002-0000-0000-000000000001'), -- phuquoc
    ('bb000002-0000-0000-0000-000000000001', 'f0000000-0007-0000-0000-000000000001'), -- resort
    ('bb000003-0000-0000-0000-000000000001', 'f0000000-0003-0000-0000-000000000001'), -- sapa
    ('bb000003-0000-0000-0000-000000000001', 'f0000000-0004-0000-0000-000000000001'), -- trekking
    ('bb000003-0000-0000-0000-000000000001', 'f0000000-0008-0000-0000-000000000001')  -- fansipan
ON CONFLICT DO NOTHING;

-- ============================================================
-- 17. POST LIKES
-- ============================================================
INSERT INTO like_post (post_id, user_id) VALUES
    ('bb000001-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000012'),
    ('bb000001-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000013'),
    ('bb000002-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000011'),
    ('bb000002-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000015'),
    ('bb000003-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000017'),
    ('bb000004-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000011'),
    ('bb000004-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000012')
ON CONFLICT DO NOTHING;

-- ============================================================
-- 18. POST SAVES
-- ============================================================
INSERT INTO save_post (post_id, user_id) VALUES
    ('bb000001-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000011'),
    ('bb000002-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000011'),
    ('bb000004-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000013')
ON CONFLICT DO NOTHING;

-- ============================================================
-- 19. EXPENSES
-- ============================================================
INSERT INTO expense (id, created_at, created_by, updated_at, updated_by,
                     name, description, type, method, amount,
                     itinerary_id, user_id)
VALUES
    ('cc000001-0001-0000-0000-000000000001', NOW(), '00000000-0000-0000-0000-000000000011', NOW(), '00000000-0000-0000-0000-000000000011',
     'Vé máy bay', 'Vé Vietjet SGN-DLI khứ hồi', 'TRANSPORT', 'BANK_TRANSFER', 1500000.00,
     'aa000001-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000011'),

    ('cc000001-0002-0000-0000-000000000001', NOW(), '00000000-0000-0000-0000-000000000012', NOW(), '00000000-0000-0000-0000-000000000012',
     'Homestay 3 đêm', 'Homestay view đồi thông', 'ACCOMMODATION', 'CASH', 1200000.00,
     'aa000001-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000012'),

    ('cc000002-0001-0000-0000-000000000001', NOW(), '00000000-0000-0000-0000-000000000012', NOW(), '00000000-0000-0000-0000-000000000012',
     'Resort Bãi Sao', 'JW Marriott 2 đêm', 'ACCOMMODATION', 'CREDIT_CARD', 3600000.00,
     'aa000002-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000012'),

    ('cc000002-0002-0000-0000-000000000001', NOW(), '00000000-0000-0000-0000-000000000015', NOW(), '00000000-0000-0000-0000-000000000015',
     'Tour lặn biển', 'Snorkeling Hòn Mây Rút', 'ACTIVITY', 'CASH', 800000.00,
     'aa000002-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000015')

ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- VERIFY
-- ============================================================
SELECT 'users'                 AS tbl, COUNT(*) FROM users
UNION ALL SELECT 'groups',             COUNT(*) FROM groups
UNION ALL SELECT 'group_member',       COUNT(*) FROM group_member
UNION ALL SELECT 'conversation',       COUNT(*) FROM conversation
UNION ALL SELECT 'conversation_member',COUNT(*) FROM conversation_member
UNION ALL SELECT 'chat_message',       COUNT(*) FROM chat_message
UNION ALL SELECT 'itinerary',          COUNT(*) FROM itinerary
UNION ALL SELECT 'post',               COUNT(*) FROM post
ORDER BY tbl;
