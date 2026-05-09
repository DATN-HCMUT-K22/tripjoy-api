-- ============================================================
-- V12: Add missing columns to travel_notebook table
-- These were added to the Java Entity but missing in the DB schema.
-- ============================================================

ALTER TABLE travel_notebook
    ADD COLUMN IF NOT EXISTS culture TEXT;

-- Note: emergency_contacts already exists from V1.
-- food and climate already exist from V9.
