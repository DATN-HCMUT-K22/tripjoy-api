-- ============================================================
-- TRIPJOY TEST DATA SEED SCRIPT
-- ============================================================
-- Chạy script này SAU KHI application đã start ít nhất 1 lần
-- (để Hibernate tạo xong tất cả bảng).
--
-- Cách chạy:
--   psql -U <username> -d <dbname> -f seed_test_data.sql
-- hoặc paste vào DBeaver / pgAdmin.
-- ============================================================

-- ============================================================
-- EXISTING USERS (đã có sẵn, KHÔNG INSERT lại)
-- ============================================================
-- admin:    f78885de-783d-4dcb-9641-648f26d1cc66
-- user1:    fa280274-c0f8-4d29-b4fc-66e3ed3a2745  (Dinh Duc)
-- user2:    f97b69a4-9d3e-4cee-9019-463037f938d9  (Ngoc Ha)
-- user3:    7ca4120c-df2c-448c-8bb5-5c9ae3a92b5a  (Huy Duc)
-- user4:    97997fd0-f418-443b-a044-a456293cc0a7  (Ngoc Chau)

BEGIN;

-- ============================================================
-- 1. GROUPS (2 groups)
-- ============================================================
INSERT INTO groups (id, created_at, created_by, updated_at, updated_by, name, description, chatbot_count, avatar, theme_color, is_pro, is_deleted, deleted_at, deleted_by)
VALUES
    ('a1111111-1111-1111-1111-111111111111', NOW(), 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745', NOW(), 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745',
     'Trip Da Lat 2026', 'Nhóm đi Đà Lạt tháng 4', 0, NULL, '#FF6B6B', false, false, NULL, NULL),

    ('a2222222-2222-2222-2222-222222222222', NOW(), 'f97b69a4-9d3e-4cee-9019-463037f938d9', NOW(), 'f97b69a4-9d3e-4cee-9019-463037f938d9',
     'Phu Quoc Summer', 'Du lịch Phú Quốc hè 2026', 0, NULL, '#4ECDC4', true, false, NULL, NULL);

-- ============================================================
-- 2. GROUP MEMBERS
-- ============================================================
-- Group 1 "Trip Da Lat": user1 (LEADER), user2, user3
INSERT INTO group_member (id, created_at, created_by, updated_at, updated_by, group_id, user_id, role, is_deleted, deleted_at, deleted_by)
VALUES
    ('b1111111-1111-1111-1111-111111111111', NOW(), 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745', NOW(), 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745',
     'a1111111-1111-1111-1111-111111111111', 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745', 'LEADER', false, NULL, NULL),

    ('b1111111-1111-1111-1111-222222222222', NOW(), 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745', NOW(), 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745',
     'a1111111-1111-1111-1111-111111111111', 'f97b69a4-9d3e-4cee-9019-463037f938d9', 'MEMBER', false, NULL, NULL),

    ('b1111111-1111-1111-1111-333333333333', NOW(), 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745', NOW(), 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745',
     'a1111111-1111-1111-1111-111111111111', '7ca4120c-df2c-448c-8bb5-5c9ae3a92b5a', 'MEMBER', false, NULL, NULL);

-- Group 2 "Phu Quoc": user2 (LEADER), user1, user3, user4
INSERT INTO group_member (id, created_at, created_by, updated_at, updated_by, group_id, user_id, role, is_deleted, deleted_at, deleted_by)
VALUES
    ('b2222222-2222-2222-2222-111111111111', NOW(), 'f97b69a4-9d3e-4cee-9019-463037f938d9', NOW(), 'f97b69a4-9d3e-4cee-9019-463037f938d9',
     'a2222222-2222-2222-2222-222222222222', 'f97b69a4-9d3e-4cee-9019-463037f938d9', 'LEADER', false, NULL, NULL),

    ('b2222222-2222-2222-2222-222222222222', NOW(), 'f97b69a4-9d3e-4cee-9019-463037f938d9', NOW(), 'f97b69a4-9d3e-4cee-9019-463037f938d9',
     'a2222222-2222-2222-2222-222222222222', 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745', 'CO_LEADER', false, NULL, NULL),

    ('b2222222-2222-2222-2222-333333333333', NOW(), 'f97b69a4-9d3e-4cee-9019-463037f938d9', NOW(), 'f97b69a4-9d3e-4cee-9019-463037f938d9',
     'a2222222-2222-2222-2222-222222222222', '7ca4120c-df2c-448c-8bb5-5c9ae3a92b5a', 'MEMBER', false, NULL, NULL),

    ('b2222222-2222-2222-2222-444444444444', NOW(), 'f97b69a4-9d3e-4cee-9019-463037f938d9', NOW(), 'f97b69a4-9d3e-4cee-9019-463037f938d9',
     'a2222222-2222-2222-2222-222222222222', '97997fd0-f418-443b-a044-a456293cc0a7', 'MEMBER', false, NULL, NULL);

-- ============================================================
-- 3. CONVERSATIONS (1 group chat + 1 direct chat)
-- ============================================================
-- Conv 1: Group chat cho "Trip Da Lat" (GROUP type)
INSERT INTO conversation (id, created_at, created_by, updated_at, updated_by, type, group_id, name, last_message_timestamp)
VALUES
    ('c1111111-1111-1111-1111-111111111111', NOW(), 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745', NOW(), 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745',
     'GROUP', 'a1111111-1111-1111-1111-111111111111', 'Trip Da Lat 2026', NOW());

-- Conv 2: Group chat cho "Phu Quoc Summer" (GROUP type)
INSERT INTO conversation (id, created_at, created_by, updated_at, updated_by, type, group_id, name, last_message_timestamp)
VALUES
    ('c2222222-2222-2222-2222-222222222222', NOW(), 'f97b69a4-9d3e-4cee-9019-463037f938d9', NOW(), 'f97b69a4-9d3e-4cee-9019-463037f938d9',
     'GROUP', 'a2222222-2222-2222-2222-222222222222', 'Phu Quoc Summer', NOW());

-- Conv 3: Direct chat giữa user1 và user4
INSERT INTO conversation (id, created_at, created_by, updated_at, updated_by, type, group_id, name, last_message_timestamp)
VALUES
    ('c3333333-3333-3333-3333-333333333333', NOW(), 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745', NOW(), 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745',
     'DIRECT', NULL, NULL, NOW());

-- ============================================================
-- 4. CONVERSATION MEMBERS
-- ============================================================
-- Conv 1 (Trip Da Lat): user1, user2, user3
INSERT INTO conversation_member (id, created_at, created_by, updated_at, updated_by, conversation_id, user_id, unread_count, is_muted, is_pinned, last_read_message_id)
VALUES
    ('d1111111-1111-1111-1111-111111111111', NOW(), 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745', NOW(), 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745',
     'c1111111-1111-1111-1111-111111111111', 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745', 0, false, false, NULL),

    ('d1111111-1111-1111-1111-222222222222', NOW(), 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745', NOW(), 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745',
     'c1111111-1111-1111-1111-111111111111', 'f97b69a4-9d3e-4cee-9019-463037f938d9', 0, false, false, NULL),

    ('d1111111-1111-1111-1111-333333333333', NOW(), 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745', NOW(), 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745',
     'c1111111-1111-1111-1111-111111111111', '7ca4120c-df2c-448c-8bb5-5c9ae3a92b5a', 0, false, false, NULL);

-- Conv 2 (Phu Quoc): user2, user1, user3, user4
INSERT INTO conversation_member (id, created_at, created_by, updated_at, updated_by, conversation_id, user_id, unread_count, is_muted, is_pinned, last_read_message_id)
VALUES
    ('d2222222-2222-2222-2222-111111111111', NOW(), 'f97b69a4-9d3e-4cee-9019-463037f938d9', NOW(), 'f97b69a4-9d3e-4cee-9019-463037f938d9',
     'c2222222-2222-2222-2222-222222222222', 'f97b69a4-9d3e-4cee-9019-463037f938d9', 0, false, false, NULL),

    ('d2222222-2222-2222-2222-222222222222', NOW(), 'f97b69a4-9d3e-4cee-9019-463037f938d9', NOW(), 'f97b69a4-9d3e-4cee-9019-463037f938d9',
     'c2222222-2222-2222-2222-222222222222', 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745', 0, false, false, NULL),

    ('d2222222-2222-2222-2222-333333333333', NOW(), 'f97b69a4-9d3e-4cee-9019-463037f938d9', NOW(), 'f97b69a4-9d3e-4cee-9019-463037f938d9',
     'c2222222-2222-2222-2222-222222222222', '7ca4120c-df2c-448c-8bb5-5c9ae3a92b5a', 0, false, false, NULL),

    ('d2222222-2222-2222-2222-444444444444', NOW(), 'f97b69a4-9d3e-4cee-9019-463037f938d9', NOW(), 'f97b69a4-9d3e-4cee-9019-463037f938d9',
     'c2222222-2222-2222-2222-222222222222', '97997fd0-f418-443b-a044-a456293cc0a7', 0, false, false, NULL);

-- Conv 3 (Direct user1 <-> user4)
INSERT INTO conversation_member (id, created_at, created_by, updated_at, updated_by, conversation_id, user_id, unread_count, is_muted, is_pinned, last_read_message_id)
VALUES
    ('d3333333-3333-3333-3333-111111111111', NOW(), 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745', NOW(), 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745',
     'c3333333-3333-3333-3333-333333333333', 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745', 0, false, false, NULL),

    ('d3333333-3333-3333-3333-222222222222', NOW(), 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745', NOW(), 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745',
     'c3333333-3333-3333-3333-333333333333', '97997fd0-f418-443b-a044-a456293cc0a7', 0, false, false, NULL);

-- ============================================================
-- 5. CHAT MESSAGES — Conv 1 "Trip Da Lat" (nhiều tin nhắn để test search)
-- ============================================================
INSERT INTO chat_message (id, created_at, created_by, updated_at, updated_by, message_type, message_content, media_url, shared_post_url, is_bot, status, is_pinned, parent_message_id, sender_id, conversation_id)
VALUES
    -- user1 gửi
    ('e1000001-0000-0000-0000-000000000001', NOW() - INTERVAL '2 hours', 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745', NOW() - INTERVAL '2 hours', 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745',
     'TEXT', 'Hello mọi người! Mình tạo nhóm đi Đà Lạt nè', NULL, NULL, false, 'SENT', false, NULL,
     'fa280274-c0f8-4d29-b4fc-66e3ed3a2745', 'c1111111-1111-1111-1111-111111111111'),

    ('e1000001-0000-0000-0000-000000000002', NOW() - INTERVAL '1 hour 55 minutes', 'f97b69a4-9d3e-4cee-9019-463037f938d9', NOW() - INTERVAL '1 hour 55 minutes', 'f97b69a4-9d3e-4cee-9019-463037f938d9',
     'TEXT', 'Oke nha! Đà Lạt mùa này đẹp lắm, hoa anh đào nở rồi', NULL, NULL, false, 'SENT', false, NULL,
     'f97b69a4-9d3e-4cee-9019-463037f938d9', 'c1111111-1111-1111-1111-111111111111'),

    ('e1000001-0000-0000-0000-000000000003', NOW() - INTERVAL '1 hour 50 minutes', '7ca4120c-df2c-448c-8bb5-5c9ae3a92b5a', NOW() - INTERVAL '1 hour 50 minutes', '7ca4120c-df2c-448c-8bb5-5c9ae3a92b5a',
     'TEXT', 'Mình cũng muốn đi! Bao giờ đi vậy?', NULL, NULL, false, 'SENT', false, NULL,
     '7ca4120c-df2c-448c-8bb5-5c9ae3a92b5a', 'c1111111-1111-1111-1111-111111111111'),

    ('e1000001-0000-0000-0000-000000000004', NOW() - INTERVAL '1 hour 45 minutes', 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745', NOW() - INTERVAL '1 hour 45 minutes', 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745',
     'TEXT', 'Dự kiến đi từ ngày 15 đến 18 tháng 4. Mọi người sắp xếp lịch nhé!', NULL, NULL, false, 'SENT', true, NULL,
     'fa280274-c0f8-4d29-b4fc-66e3ed3a2745', 'c1111111-1111-1111-1111-111111111111'),

    ('e1000001-0000-0000-0000-000000000005', NOW() - INTERVAL '1 hour 40 minutes', 'f97b69a4-9d3e-4cee-9019-463037f938d9', NOW() - INTERVAL '1 hour 40 minutes', 'f97b69a4-9d3e-4cee-9019-463037f938d9',
     'TEXT', 'Khách sạn mình book ở đâu? Gần hồ Xuân Hương thì tốt', NULL, NULL, false, 'SENT', false, NULL,
     'f97b69a4-9d3e-4cee-9019-463037f938d9', 'c1111111-1111-1111-1111-111111111111'),

    ('e1000001-0000-0000-0000-000000000006', NOW() - INTERVAL '1 hour 35 minutes', 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745', NOW() - INTERVAL '1 hour 35 minutes', 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745',
     'TEXT', 'Mình tìm được mấy homestay trên Booking rồi, giá khoảng 500k/đêm thôi', NULL, NULL, false, 'SENT', false, NULL,
     'fa280274-c0f8-4d29-b4fc-66e3ed3a2745', 'c1111111-1111-1111-1111-111111111111'),

    ('e1000001-0000-0000-0000-000000000007', NOW() - INTERVAL '1 hour 30 minutes', '7ca4120c-df2c-448c-8bb5-5c9ae3a92b5a', NOW() - INTERVAL '1 hour 30 minutes', '7ca4120c-df2c-448c-8bb5-5c9ae3a92b5a',
     'TEXT', 'Ngon! Nhớ book chỗ nào có view đẹp nha. Mình thích chụp ảnh sáng sớm', NULL, NULL, false, 'SENT', false, NULL,
     '7ca4120c-df2c-448c-8bb5-5c9ae3a92b5a', 'c1111111-1111-1111-1111-111111111111'),

    ('e1000001-0000-0000-0000-000000000008', NOW() - INTERVAL '1 hour 25 minutes', 'f97b69a4-9d3e-4cee-9019-463037f938d9', NOW() - INTERVAL '1 hour 25 minutes', 'f97b69a4-9d3e-4cee-9019-463037f938d9',
     'TEXT', 'Có ai biết quán cà phê nào ngon ở Đà Lạt không? Mình muốn đi check-in', NULL, NULL, false, 'SENT', false, NULL,
     'f97b69a4-9d3e-4cee-9019-463037f938d9', 'c1111111-1111-1111-1111-111111111111'),

    ('e1000001-0000-0000-0000-000000000009', NOW() - INTERVAL '1 hour 20 minutes', 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745', NOW() - INTERVAL '1 hour 20 minutes', 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745',
     'TEXT', 'Quán The Married Beans ngon lắm, view cũng đẹp. Rồi còn quán Bích Câu nữa', NULL, NULL, false, 'SENT', false, NULL,
     'fa280274-c0f8-4d29-b4fc-66e3ed3a2745', 'c1111111-1111-1111-1111-111111111111'),

    ('e1000001-0000-0000-0000-000000000010', NOW() - INTERVAL '1 hour 15 minutes', '7ca4120c-df2c-448c-8bb5-5c9ae3a92b5a', NOW() - INTERVAL '1 hour 15 minutes', '7ca4120c-df2c-448c-8bb5-5c9ae3a92b5a',
     'TEXT', 'Mọi người nhớ mang áo ấm nha, Đà Lạt buổi tối lạnh lắm!', NULL, NULL, false, 'SENT', true, NULL,
     '7ca4120c-df2c-448c-8bb5-5c9ae3a92b5a', 'c1111111-1111-1111-1111-111111111111'),

    -- Reply message (reply to message 8 about café)
    ('e1000001-0000-0000-0000-000000000011', NOW() - INTERVAL '1 hour 10 minutes', 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745', NOW() - INTERVAL '1 hour 10 minutes', 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745',
     'TEXT', 'Mình sẽ lên lịch trình chi tiết trên TripJoy nha, mọi người vào xem rồi góp ý', NULL, NULL, false, 'SENT', false,
     'e1000001-0000-0000-0000-000000000008',
     'fa280274-c0f8-4d29-b4fc-66e3ed3a2745', 'c1111111-1111-1111-1111-111111111111'),

    ('e1000001-0000-0000-0000-000000000012', NOW() - INTERVAL '1 hour 5 minutes', 'f97b69a4-9d3e-4cee-9019-463037f938d9', NOW() - INTERVAL '1 hour 5 minutes', 'f97b69a4-9d3e-4cee-9019-463037f938d9',
     'TEXT', 'Budget mỗi người khoảng bao nhiêu? Mình dự tính 3 triệu cho 4 ngày', NULL, NULL, false, 'SENT', false, NULL,
     'f97b69a4-9d3e-4cee-9019-463037f938d9', 'c1111111-1111-1111-1111-111111111111'),

    ('e1000001-0000-0000-0000-000000000013', NOW() - INTERVAL '1 hour', 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745', NOW() - INTERVAL '1 hour', 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745',
     'TEXT', '3 triệu là hợp lý đó, chưa tính vé máy bay nha. Mình book vé trên Vietjet luôn', NULL, NULL, false, 'SENT', false, NULL,
     'fa280274-c0f8-4d29-b4fc-66e3ed3a2745', 'c1111111-1111-1111-1111-111111111111'),

    ('e1000001-0000-0000-0000-000000000014', NOW() - INTERVAL '55 minutes', '7ca4120c-df2c-448c-8bb5-5c9ae3a92b5a', NOW() - INTERVAL '55 minutes', '7ca4120c-df2c-448c-8bb5-5c9ae3a92b5a',
     'TEXT', 'Mình đi xe máy từ Sài Gòn lên được không ta? Đi đường đèo vui lắm', NULL, NULL, false, 'SENT', false, NULL,
     '7ca4120c-df2c-448c-8bb5-5c9ae3a92b5a', 'c1111111-1111-1111-1111-111111111111'),

    ('e1000001-0000-0000-0000-000000000015', NOW() - INTERVAL '50 minutes', 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745', NOW() - INTERVAL '50 minutes', 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745',
     'TEXT', 'Cũng được nhưng mà đi xa lắm, tầm 300km. Bay chỉ 1 tiếng thôi', NULL, NULL, false, 'SENT', false, NULL,
     'fa280274-c0f8-4d29-b4fc-66e3ed3a2745', 'c1111111-1111-1111-1111-111111111111');

-- ============================================================
-- 6. CHAT MESSAGES — Conv 2 "Phu Quoc Summer"
-- ============================================================
INSERT INTO chat_message (id, created_at, created_by, updated_at, updated_by, message_type, message_content, media_url, shared_post_url, is_bot, status, is_pinned, parent_message_id, sender_id, conversation_id)
VALUES
    ('e2000001-0000-0000-0000-000000000001', NOW() - INTERVAL '3 hours', 'f97b69a4-9d3e-4cee-9019-463037f938d9', NOW() - INTERVAL '3 hours', 'f97b69a4-9d3e-4cee-9019-463037f938d9',
     'TEXT', 'Chào team Phú Quốc! Mùa hè này mình đi biển nha', NULL, NULL, false, 'SENT', false, NULL,
     'f97b69a4-9d3e-4cee-9019-463037f938d9', 'c2222222-2222-2222-2222-222222222222'),

    ('e2000001-0000-0000-0000-000000000002', NOW() - INTERVAL '2 hours 55 minutes', 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745', NOW() - INTERVAL '2 hours 55 minutes', 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745',
     'TEXT', 'Phú Quốc xịn quá! Mình muốn đi snorkeling lặn biển', NULL, NULL, false, 'SENT', false, NULL,
     'fa280274-c0f8-4d29-b4fc-66e3ed3a2745', 'c2222222-2222-2222-2222-222222222222'),

    ('e2000001-0000-0000-0000-000000000003', NOW() - INTERVAL '2 hours 50 minutes', '7ca4120c-df2c-448c-8bb5-5c9ae3a92b5a', NOW() - INTERVAL '2 hours 50 minutes', '7ca4120c-df2c-448c-8bb5-5c9ae3a92b5a',
     'TEXT', 'Count me in! Phú Quốc có VinWonders nữa nè, ai muốn đi không?', NULL, NULL, false, 'SENT', false, NULL,
     '7ca4120c-df2c-448c-8bb5-5c9ae3a92b5a', 'c2222222-2222-2222-2222-222222222222'),

    ('e2000001-0000-0000-0000-000000000004', NOW() - INTERVAL '2 hours 45 minutes', '97997fd0-f418-443b-a044-a456293cc0a7', NOW() - INTERVAL '2 hours 45 minutes', '97997fd0-f418-443b-a044-a456293cc0a7',
     'TEXT', 'Mình cũng muốn đi! Booking resort hay khách sạn vậy mọi người?', NULL, NULL, false, 'SENT', false, NULL,
     '97997fd0-f418-443b-a044-a456293cc0a7', 'c2222222-2222-2222-2222-222222222222'),

    ('e2000001-0000-0000-0000-000000000005', NOW() - INTERVAL '2 hours 40 minutes', 'f97b69a4-9d3e-4cee-9019-463037f938d9', NOW() - INTERVAL '2 hours 40 minutes', 'f97b69a4-9d3e-4cee-9019-463037f938d9',
     'TEXT', 'Book resort luôn cho sang! Mình tìm thấy resort 4 sao ở Bãi Sao, giá 1.2 triệu/đêm chia 2 phòng', NULL, NULL, false, 'SENT', true, NULL,
     'f97b69a4-9d3e-4cee-9019-463037f938d9', 'c2222222-2222-2222-2222-222222222222'),

    ('e2000001-0000-0000-0000-000000000006', NOW() - INTERVAL '2 hours 35 minutes', 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745', NOW() - INTERVAL '2 hours 35 minutes', 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745',
     'TEXT', 'Ngon! Nhớ kiểm tra review trên TripAdvisor trước nha', NULL, NULL, false, 'SENT', false, NULL,
     'fa280274-c0f8-4d29-b4fc-66e3ed3a2745', 'c2222222-2222-2222-2222-222222222222'),

    ('e2000001-0000-0000-0000-000000000007', NOW() - INTERVAL '2 hours 30 minutes', '97997fd0-f418-443b-a044-a456293cc0a7', NOW() - INTERVAL '2 hours 30 minutes', '97997fd0-f418-443b-a044-a456293cc0a7',
     'TEXT', 'Mình sẽ mang theo kem chống nắng cho cả nhóm luôn. SPF 50+ nha!', NULL, NULL, false, 'SENT', false, NULL,
     '97997fd0-f418-443b-a044-a456293cc0a7', 'c2222222-2222-2222-2222-222222222222'),

    ('e2000001-0000-0000-0000-000000000008', NOW() - INTERVAL '2 hours 25 minutes', '7ca4120c-df2c-448c-8bb5-5c9ae3a92b5a', NOW() - INTERVAL '2 hours 25 minutes', '7ca4120c-df2c-448c-8bb5-5c9ae3a92b5a',
     'TEXT', 'Nhớ mang theo đồ bơi nữa nha. Biển Phú Quốc đẹp nhất lúc hoàng hôn', NULL, NULL, false, 'SENT', false, NULL,
     '7ca4120c-df2c-448c-8bb5-5c9ae3a92b5a', 'c2222222-2222-2222-2222-222222222222'),

    ('e2000001-0000-0000-0000-000000000009', NOW() - INTERVAL '2 hours 20 minutes', 'f97b69a4-9d3e-4cee-9019-463037f938d9', NOW() - INTERVAL '2 hours 20 minutes', 'f97b69a4-9d3e-4cee-9019-463037f938d9',
     'TEXT', 'Ai rành hải sản không? Mình muốn ăn hải sản tươi sống ở chợ đêm Phú Quốc', NULL, NULL, false, 'SENT', false, NULL,
     'f97b69a4-9d3e-4cee-9019-463037f938d9', 'c2222222-2222-2222-2222-222222222222'),

    ('e2000001-0000-0000-0000-000000000010', NOW() - INTERVAL '2 hours 15 minutes', 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745', NOW() - INTERVAL '2 hours 15 minutes', 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745',
     'TEXT', 'Chợ đêm Phú Quốc nổi tiếng lắm! Mực nướng, ghẹ hấp, sò điệp nướng mỡ hành', NULL, NULL, false, 'SENT', false, NULL,
     'fa280274-c0f8-4d29-b4fc-66e3ed3a2745', 'c2222222-2222-2222-2222-222222222222');

-- ============================================================
-- 7. CHAT MESSAGES — Conv 3 (Direct: user1 <-> user4)
-- ============================================================
INSERT INTO chat_message (id, created_at, created_by, updated_at, updated_by, message_type, message_content, media_url, shared_post_url, is_bot, status, is_pinned, parent_message_id, sender_id, conversation_id)
VALUES
    ('e3000001-0000-0000-0000-000000000001', NOW() - INTERVAL '30 minutes', 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745', NOW() - INTERVAL '30 minutes', 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745',
     'TEXT', 'Hey Chau! Bạn có muốn join trip Đà Lạt không?', NULL, NULL, false, 'SENT', false, NULL,
     'fa280274-c0f8-4d29-b4fc-66e3ed3a2745', 'c3333333-3333-3333-3333-333333333333'),

    ('e3000001-0000-0000-0000-000000000002', NOW() - INTERVAL '25 minutes', '97997fd0-f418-443b-a044-a456293cc0a7', NOW() - INTERVAL '25 minutes', '97997fd0-f418-443b-a044-a456293cc0a7',
     'TEXT', 'Oke luôn! Bao giờ đi vậy Duc?', NULL, NULL, false, 'SENT', false, NULL,
     '97997fd0-f418-443b-a044-a456293cc0a7', 'c3333333-3333-3333-3333-333333333333'),

    ('e3000001-0000-0000-0000-000000000003', NOW() - INTERVAL '20 minutes', 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745', NOW() - INTERVAL '20 minutes', 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745',
     'TEXT', 'Tháng 4, từ 15 đến 18. Mình add bạn vô nhóm nha!', NULL, NULL, false, 'SENT', false, NULL,
     'fa280274-c0f8-4d29-b4fc-66e3ed3a2745', 'c3333333-3333-3333-3333-333333333333'),

    ('e3000001-0000-0000-0000-000000000004', NOW() - INTERVAL '15 minutes', '97997fd0-f418-443b-a044-a456293cc0a7', NOW() - INTERVAL '15 minutes', '97997fd0-f418-443b-a044-a456293cc0a7',
     'TEXT', 'Tuyệt vời! Mình nghe nói Đà Lạt có quán bánh tráng nướng ngon lắm', NULL, NULL, false, 'SENT', false, NULL,
     '97997fd0-f418-443b-a044-a456293cc0a7', 'c3333333-3333-3333-3333-333333333333'),

    ('e3000001-0000-0000-0000-000000000005', NOW() - INTERVAL '10 minutes', 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745', NOW() - INTERVAL '10 minutes', 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745',
     'TEXT', 'Đúng rồi! Bánh tráng nướng Đà Lạt là must try luôn 🔥', NULL, NULL, false, 'SENT', false, NULL,
     'fa280274-c0f8-4d29-b4fc-66e3ed3a2745', 'c3333333-3333-3333-3333-333333333333');

-- ============================================================
-- 8. MESSAGE LIKES (vài tin nhắn được like)
-- ============================================================
INSERT INTO like_chat_message (chat_message_id, user_id)
VALUES
    -- Tin nhắn lịch trình (pinned) được cả 3 like
    ('e1000001-0000-0000-0000-000000000004', 'f97b69a4-9d3e-4cee-9019-463037f938d9'),
    ('e1000001-0000-0000-0000-000000000004', '7ca4120c-df2c-448c-8bb5-5c9ae3a92b5a'),

    -- Tin nhắn về homestay
    ('e1000001-0000-0000-0000-000000000006', 'f97b69a4-9d3e-4cee-9019-463037f938d9'),

    -- Tin nhắn resort Phú Quốc được like
    ('e2000001-0000-0000-0000-000000000005', 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745'),
    ('e2000001-0000-0000-0000-000000000005', '7ca4120c-df2c-448c-8bb5-5c9ae3a92b5a'),
    ('e2000001-0000-0000-0000-000000000005', '97997fd0-f418-443b-a044-a456293cc0a7');

COMMIT;

-- ============================================================
-- VERIFY: Kiểm tra dữ liệu đã insert
-- ============================================================
SELECT 'groups' AS table_name, COUNT(*) AS row_count FROM groups
UNION ALL SELECT 'group_member', COUNT(*) FROM group_member
UNION ALL SELECT 'conversation', COUNT(*) FROM conversation
UNION ALL SELECT 'conversation_member', COUNT(*) FROM conversation_member
UNION ALL SELECT 'chat_message', COUNT(*) FROM chat_message
UNION ALL SELECT 'like_chat_message', COUNT(*) FROM like_chat_message
ORDER BY table_name;

-- ============================================================
-- TEST SEARCH API (sau khi insert):
-- Login bằng user1 (StrongP@ss123), lấy token rồi gọi:
--
-- GET /api/v1/conversations/c1111111-1111-1111-1111-111111111111/messages/search?q=Đà Lạt
-- GET /api/v1/conversations/c1111111-1111-1111-1111-111111111111/messages/search?q=homestay
-- GET /api/v1/conversations/c1111111-1111-1111-1111-111111111111/messages/search?q=cà phê
-- GET /api/v1/conversations/c2222222-2222-2222-2222-222222222222/messages/search?q=resort
-- GET /api/v1/conversations/c2222222-2222-2222-2222-222222222222/messages/search?q=hải sản
-- ============================================================

-- ============================================================

-- 9. ITINERARIES (Cho bài test Search Multi-Filters)
-- ============================================================
INSERT INTO itinerary (id, name, description, start_date, end_date, people_quantity, budget_estimate, status, group_id, user_id, origin, destination, is_deleted, created_at, created_by, updated_at, updated_by)
VALUES
    -- Iti 1: Đà Lạt, 4 ngày, Budget 3tr, 4 người (Link với Group 1)
    ('f1111111-1111-1111-1111-111111111111', 'Đà Lạt Phượt Cùng Nhau', 'Chuyến đi tránh nóng tháng 4', NOW() + INTERVAL '30 days', NOW() + INTERVAL '34 days', 4, 3000000.00, 'PLANNING', 'a1111111-1111-1111-1111-111111111111', 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745', NULL, NULL, false, NOW(), 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745', NOW(), 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745'),
    
    -- Iti 2: Phú Quốc, 3 ngày, Budget 8tr, 2 người
    ('f2222222-2222-2222-2222-222222222222', 'Phú Quốc Resort Nghỉ Dưỡng', 'Chill hè bãi biển Phú Quốc', NOW() + INTERVAL '45 days', NOW() + INTERVAL '48 days', 2, 8000000.00, 'PLANNING', 'a2222222-2222-2222-2222-222222222222', 'f97b69a4-9d3e-4cee-9019-463037f938d9', NULL, NULL, false, NOW(), 'f97b69a4-9d3e-4cee-9019-463037f938d9', NOW(), 'f97b69a4-9d3e-4cee-9019-463037f938d9'),

    -- Iti 3: Sapa Trekking, 5 ngày, Budget 5tr, 6 người
    ('f3333333-3333-3333-3333-333333333333', 'Trekking Fansipan', 'Leo núi và ngắm mây Sapa', NOW() + INTERVAL '60 days', NOW() + INTERVAL '65 days', 6, 5000000.00, 'PLANNING', NULL, '7ca4120c-df2c-448c-8bb5-5c9ae3a92b5a', NULL, NULL, false, NOW(), '7ca4120c-df2c-448c-8bb5-5c9ae3a92b5a', NOW(), '7ca4120c-df2c-448c-8bb5-5c9ae3a92b5a')
ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- 10. POSTS (Test Full-Text Search và Trigram)
-- ============================================================
INSERT INTO post (id, content, share_quantity, itinerary_id, creator_id, is_deleted, created_at, created_by, updated_at, updated_by)
VALUES
    -- Post 1 (Có chứa chữ Đà Lạt, phượt, sương mù - Test FTS)
    ('d1111111-1111-1111-1111-111111111111', 'Tuần trước mình mới đi phượt Đà Lạt về, sương mù siêu lãng mạn luôn các bạn ơi. Thời tiết có chút lạnh nhưng cafe thì đỉnh chóp. Giá rẻ bèo chỉ 3 triệu cho 4 ngày. Ai cần kinh nghiệm thì inbox nha.', 12, 'f1111111-1111-1111-1111-111111111111', 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745', false, NOW() - INTERVAL '3 days', 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745', NOW(), 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745'),
    
    -- Post 2 (Có chữ Phú Quốc, biển, resort - Test FTS & Budget Itinerary)
    ('d2222222-2222-2222-2222-222222222222', 'Review chuyến nghỉ dưỡng Phú Quốc tại JW Marriott. Bãi biển xanh ngát, hải sản ở chợ đêm thì tươi ngon tuyệt vời. Budget lần này hơi cao xíu do mình chọn ở resort, nhưng rất đáng đồng tiền bát gạo.', 45, 'f2222222-2222-2222-2222-222222222222', 'f97b69a4-9d3e-4cee-9019-463037f938d9', false, NOW() - INTERVAL '2 days', 'f97b69a4-9d3e-4cee-9019-463037f938d9', NOW(), 'f97b69a4-9d3e-4cee-9019-463037f938d9'),

    -- Post 3 (Sapa, leo núi, trekking - Test Trigram gõ sai lỗi chính tả sa pa)
    ('d3333333-3333-3333-3333-333333333333', 'Tìm đồng đội đi Trekking Fansipan Sapa tháng tới đây cả nhà ơi. Lịch trình 5 ngày leo hộc bơ ngắm biển mây rực rỡ. Ai có thể lực tốt thì join cùng team mình nha, chi phí chia đều siêu bình dân.', 5, 'f3333333-3333-3333-3333-333333333333', '7ca4120c-df2c-448c-8bb5-5c9ae3a92b5a', false, NOW() - INTERVAL '1 day', '7ca4120c-df2c-448c-8bb5-5c9ae3a92b5a', NOW(), '7ca4120c-df2c-448c-8bb5-5c9ae3a92b5a')
ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- 11. POST HASHTAGS (Để test Exact Match với ILIKE)
-- ============================================================
INSERT INTO post_hashtag (id, post_id, hashtag, created_at)
VALUES
    (gen_random_uuid(), 'd1111111-1111-1111-1111-111111111111', 'dalat', NOW()),
    (gen_random_uuid(), 'd1111111-1111-1111-1111-111111111111', 'phuot', NOW()),
    (gen_random_uuid(), 'd1111111-1111-1111-1111-111111111111', 'review', NOW()),
    
    (gen_random_uuid(), 'd2222222-2222-2222-2222-222222222222', 'phuquoc', NOW()),
    (gen_random_uuid(), 'd2222222-2222-2222-2222-222222222222', 'resort', NOW()),
    (gen_random_uuid(), 'd2222222-2222-2222-2222-222222222222', 'chill', NOW()),

    (gen_random_uuid(), 'd3333333-3333-3333-3333-333333333333', 'sapa', NOW()),
    (gen_random_uuid(), 'd3333333-3333-3333-3333-333333333333', 'trekking', NOW()),
    (gen_random_uuid(), 'd3333333-3333-3333-3333-333333333333', 'fansipan', NOW())
ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- 12. SAVE / LIKE POST (Test Context isLiked / isSaved)
-- ============================================================
INSERT INTO like_post (post_id, user_id)
VALUES
    ('d1111111-1111-1111-1111-111111111111', 'f97b69a4-9d3e-4cee-9019-463037f938d9'),
    ('d1111111-1111-1111-1111-111111111111', '7ca4120c-df2c-448c-8bb5-5c9ae3a92b5a'),
    ('d2222222-2222-2222-2222-222222222222', 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745');

INSERT INTO save_post (post_id, user_id)
VALUES
    ('d3333333-3333-3333-3333-333333333333', 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745'),
    ('d1111111-1111-1111-1111-111111111111', 'fa280274-c0f8-4d29-b4fc-66e3ed3a2745');
    
