-- ============================================================
-- V15 — Add raw location fallback fields to trip_item table
-- These fields store a "snapshot" of the AI's suggested location
-- to ensure the UI remains populated even if the full Location 
-- enrichment process is delayed or fails.
-- ============================================================

ALTER TABLE trip_item
    ADD COLUMN IF NOT EXISTS raw_location_name TEXT,
    ADD COLUMN IF NOT EXISTS raw_place_id      VARCHAR(500);

-- Indexing place_id for faster lookup during enrichment
CREATE INDEX IF NOT EXISTS idx_trip_item_raw_place ON trip_item(raw_place_id);
