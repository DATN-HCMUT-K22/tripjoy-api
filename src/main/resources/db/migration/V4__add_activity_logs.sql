-- ============================================================
-- V4 — Add activity_logs table
-- ActivityLog entity was missing from V1 migration.
-- V1 was already applied, so this separate migration creates the table.
-- ============================================================

CREATE TABLE IF NOT EXISTS activity_logs (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at  TIMESTAMP,
    created_by  UUID,
    updated_at  TIMESTAMP,
    updated_by  UUID,

    user_id     UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    action      VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50),
    entity_id   VARCHAR(255),
    metadata    TEXT,
    ip_address  VARCHAR(45),
    user_agent  TEXT
);

CREATE INDEX IF NOT EXISTS idx_user_action     ON activity_logs(user_id, action, created_at);
CREATE INDEX IF NOT EXISTS idx_activity_entity ON activity_logs(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_created_at      ON activity_logs(created_at);
