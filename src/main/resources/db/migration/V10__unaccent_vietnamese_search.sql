-- ============================================================
-- V10 — Fix Vietnamese Accent-Insensitive Search
--
-- Problem: All search queries fail for unaccented Vietnamese input.
--          e.g. "hoi an" does NOT match "Hội An"
--
-- Fix: IMMUTABLE wrapper f_unaccent() calling public.unaccent()
--      with explicit schema qualification to ensure it resolves
--      correctly regardless of the session search_path.
-- ============================================================

-- ============================================================
-- 1. Enable unaccent extension in public schema (idempotent)
-- ============================================================
CREATE EXTENSION IF NOT EXISTS unaccent SCHEMA public;

-- ============================================================
-- 2. IMMUTABLE wrapper with explicit schema-qualified call
--    Uses plpgsql to prevent optimizer inlining.
--    Calls public.unaccent() explicitly so search_path doesn't matter.
-- ============================================================
CREATE OR REPLACE FUNCTION f_unaccent(text)
    RETURNS text
    LANGUAGE plpgsql IMMUTABLE PARALLEL SAFE STRICT
AS $$
BEGIN
    RETURN public.unaccent($1);
END;
$$;

-- ============================================================
-- 3. Rebuild Location FTS trigger (strips accents before tokenizing)
-- ============================================================
CREATE OR REPLACE FUNCTION location_search_vector_update() RETURNS trigger AS $$
BEGIN
    NEW.search_vector :=
        setweight(to_tsvector('simple', f_unaccent(coalesce(NEW.name, ''))),           'A') ||
        setweight(to_tsvector('simple', f_unaccent(coalesce(NEW.name_en, ''))),        'B') ||
        setweight(to_tsvector('simple', f_unaccent(coalesce(NEW.full_address, ''))),   'B') ||
        setweight(to_tsvector('simple', f_unaccent(coalesce(NEW.poi_categories, ''))), 'D');
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

-- ============================================================
-- 4. Backfill existing Location rows (fires the trigger above)
-- ============================================================
UPDATE location SET id = id;

-- ============================================================
-- 5. GIN index on Post content using IMMUTABLE f_unaccent()
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_post_content_unaccent
    ON post USING GIN (to_tsvector('simple', f_unaccent(coalesce(content, ''))));

-- ============================================================
-- 6. B-Tree index for Group name (accent-insensitive)
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_group_name_unaccent
    ON groups (lower(f_unaccent(name)));

-- ============================================================
-- After this migration:
--   "hoi an"  matches "Hội An"
--   "da nang" matches "Đà Nẵng"
--   "nguyen"  matches "Nguyễn"
-- ============================================================
