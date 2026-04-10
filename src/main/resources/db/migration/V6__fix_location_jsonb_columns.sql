-- ============================================================
-- V6 — Fix column types in 'location' table
-- Several columns were created as TEXT in V1 but the Location entity
-- uses @JdbcTypeCode(SqlTypes.JSON) which requires JSONB in PostgreSQL.
-- ============================================================

-- ALTER opening_hours: TEXT -> JSONB
ALTER TABLE location
    ALTER COLUMN opening_hours TYPE JSONB USING opening_hours::JSONB;

-- ALTER viewport: TEXT -> JSONB (also mapped with @JdbcTypeCode(SqlTypes.JSON))
ALTER TABLE location
    ALTER COLUMN viewport TYPE JSONB USING viewport::JSONB;

-- ALTER raw_response: TEXT -> JSONB (also mapped with @JdbcTypeCode(SqlTypes.JSON))
ALTER TABLE location
    ALTER COLUMN raw_response TYPE JSONB USING raw_response::JSONB;
