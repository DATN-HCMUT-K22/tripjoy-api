-- ============================================================
-- V5 - Add rating and review columns to trip_item table
-- ============================================================

ALTER TABLE trip_item ADD COLUMN rating INTEGER;
ALTER TABLE trip_item ADD COLUMN review TEXT;
