-- ============================================================
-- V11 — Add GIN index for ChatMessage content using f_unaccent
--
-- Problem: Chat message full-text search was optimized to use f_unaccent()
--          but lacked the corresponding GIN index, leading to sequential scans.
--
-- Fix: Create a GIN index on the chat_message table to support
--      accent-insensitive full-text search.
-- ============================================================

CREATE INDEX IF NOT EXISTS idx_chat_message_content_unaccent
    ON chat_message USING GIN (to_tsvector('simple', f_unaccent(coalesce(message_content, ''))));
