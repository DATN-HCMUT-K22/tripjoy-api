-- ============================================================
-- V8 — Add visibility to post table
-- ============================================================

ALTER TABLE post 
ADD COLUMN visibility VARCHAR(20) NOT NULL DEFAULT 'PUBLIC';
