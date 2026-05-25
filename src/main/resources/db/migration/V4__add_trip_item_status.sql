-- ============================================================
-- V4 - Add status column to trip_item table
-- ============================================================

ALTER TABLE trip_item ADD COLUMN status VARCHAR(50) NOT NULL DEFAULT 'PENDING';
