-- ============================================================
-- V5 — Add missing tables: feedback, report_content,
--       handle_report_content, moderation_action
-- These entities existed in Java code but were not in V1 migration.
-- ============================================================

-- Feedback (user-to-user feedback, e.g. trip reviews)
-- Hibernate default table name: "feedback"
CREATE TABLE IF NOT EXISTS feedback (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at  TIMESTAMP,
    created_by  UUID,
    updated_at  TIMESTAMP,
    updated_by  UUID,
    type        VARCHAR(255),
    title       VARCHAR(255),
    content     TEXT,
    status      VARCHAR(255),
    sender_id   UUID        NOT NULL REFERENCES users(id),
    receiver_id UUID        NOT NULL REFERENCES users(id)
);

-- ReportContent (reported posts/comments/etc)
-- Hibernate default table name: "report_content"
CREATE TABLE IF NOT EXISTS report_content (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at      TIMESTAMP,
    created_by      UUID,
    updated_at      TIMESTAMP,
    updated_by      UUID,
    content_type    VARCHAR(255),
    text            TEXT,
    media_url       VARCHAR(500),
    status          VARCHAR(255)
);

-- HandleReportContent (admin/BA handling of reports)
-- Hibernate default table name: "handle_report_content"
CREATE TABLE IF NOT EXISTS handle_report_content (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at          TIMESTAMP,
    created_by          UUID,
    updated_at          TIMESTAMP,
    updated_by          UUID,
    report_type         VARCHAR(255),
    description         TEXT,
    report_content_id   UUID        NOT NULL REFERENCES report_content(id),
    ba_id               UUID        NOT NULL REFERENCES users(id)
);

-- ModerationAction (moderation/ban actions by BA)
-- Hibernate default table name: "moderation_action"
CREATE TABLE IF NOT EXISTS moderation_action (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at  TIMESTAMP,
    created_by  UUID,
    updated_at  TIMESTAMP,
    updated_by  UUID,
    action_type VARCHAR(255),
    note        TEXT,
    user_id     UUID        NOT NULL REFERENCES users(id),
    ba_id       UUID        NOT NULL REFERENCES users(id)
);
