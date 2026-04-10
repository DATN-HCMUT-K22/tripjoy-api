-- ============================================================
-- V9: Add AI-generated columns to travel_notebook table
-- 
-- Thêm 2 cột do AI Notebook feature sinh ra:
--   food    → nội dung ẩm thực địa phương
--   climate → khí hậu + lời khuyên trang phục theo mùa
-- ============================================================

ALTER TABLE travel_notebook
    ADD COLUMN IF NOT EXISTS food    TEXT,
    ADD COLUMN IF NOT EXISTS climate TEXT;
