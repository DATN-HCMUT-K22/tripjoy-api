-- ============================================================
-- V2 — Location Two-Tier Architecture
-- Adds new columns, indexes, GIN full-text search, and trigger
-- Migrated from: src/main/resources/seed/migration/V1_location_two_tier_alter.sql
-- NOTE: V1__init_schema.sql already creates location with all columns.
-- This file is intentionally a no-op (idempotent) for fresh installs.
-- It runs ALTER TABLE ... ADD COLUMN IF NOT EXISTS for existing DBs.
-- ============================================================

-- ============================================================
-- 1. SPATIAL INDEX (PostGIS)
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_location_postgis_geom
    ON location USING GIST (coordinates);

-- ============================================================
-- 2. B-TREE INDEXES FOR FREQUENT FILTERS
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_location_provider_id     ON location(provider_id);
CREATE INDEX IF NOT EXISTS idx_location_type            ON location(location_type);
CREATE INDEX IF NOT EXISTS idx_location_type_verified   ON location(location_type, is_verified);
CREATE INDEX IF NOT EXISTS idx_location_country_type    ON location(country_code, location_type);
CREATE INDEX IF NOT EXISTS idx_location_usage_count     ON location(usage_count DESC);
CREATE INDEX IF NOT EXISTS idx_location_coordinates     ON location(latitude, longitude);

-- ============================================================
-- 3. FULL-TEXT SEARCH (GIN INDEX + TRIGGER)
-- ============================================================

-- 3.1 GIN index
CREATE INDEX IF NOT EXISTS idx_location_search_vector
    ON location USING GIN (search_vector);

-- 3.2 Trigger function
CREATE OR REPLACE FUNCTION location_search_vector_update() RETURNS trigger AS $$
BEGIN
    NEW.search_vector :=
        setweight(to_tsvector('simple', coalesce(NEW.name, '')),         'A') ||
        setweight(to_tsvector('simple', coalesce(NEW.name_en, '')),      'B') ||
        setweight(to_tsvector('simple', coalesce(NEW.full_address, '')), 'B') ||
        setweight(to_tsvector('simple', coalesce(NEW.poi_categories, '')), 'D');
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

-- 3.3 Attach trigger
DROP TRIGGER IF EXISTS trg_location_search_vector_update ON location;
CREATE TRIGGER trg_location_search_vector_update
    BEFORE INSERT OR UPDATE ON location
    FOR EACH ROW EXECUTE FUNCTION location_search_vector_update();

-- 3.4 Backfill existing rows
UPDATE location SET id = id WHERE search_vector IS NULL;
