-- ============================================================
-- V16 — Remove raw location fallback fields from trip_item
-- Reason: Enrichment process is now stable, fallback fields are no longer needed.
-- ============================================================

-- Drop index first
DROP INDEX IF EXISTS idx_trip_item_raw_place;

-- Drop columns
ALTER TABLE trip_item
    DROP COLUMN IF EXISTS raw_location_name,
    DROP COLUMN IF EXISTS raw_place_id;
