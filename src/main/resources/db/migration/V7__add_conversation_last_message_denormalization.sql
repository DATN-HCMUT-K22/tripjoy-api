-- ============================================================
-- V7 — Add denormalization columns to conversation table
-- Purpose: Store last message info directly for inbox performance
-- ============================================================

ALTER TABLE conversation 
ADD COLUMN last_message_id          UUID,
ADD COLUMN last_message_content     TEXT,
ADD COLUMN last_message_type        VARCHAR(50),
ADD COLUMN last_message_sender_id   UUID,
ADD COLUMN last_message_sender_name VARCHAR(255),
ADD COLUMN last_message_sender_avatar VARCHAR(500);
