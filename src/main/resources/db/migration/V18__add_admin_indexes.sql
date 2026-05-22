-- =====================================================
-- Admin System Performance Indexes
-- Migration: V18__add_admin_indexes.sql
-- Date: 2026-05-21
-- Purpose: Add indexes for report, moderation, and analytics queries
-- =====================================================

-- ========== REPORT_CONTENT INDEXES ==========

-- Index for filtering reports by status (PENDING, PROCESSED, DISMISSED)
CREATE INDEX IF NOT EXISTS idx_report_content_status
ON report_content(status);

-- Index for filtering reports by content type (POST, COMMENT)
CREATE INDEX IF NOT EXISTS idx_report_content_type
ON report_content(content_type);

-- Index for date range queries (analytics, filtering)
CREATE INDEX IF NOT EXISTS idx_report_content_created_at
ON report_content(created_at);

-- Composite index for common filter combinations (status + created_at)
CREATE INDEX IF NOT EXISTS idx_report_content_status_created_at
ON report_content(status, created_at);

-- ========== HANDLE_REPORT_CONTENT INDEXES ==========

-- Index for filtering by report type (SPAM, HARASSMENT, etc.)
CREATE INDEX IF NOT EXISTS idx_handle_report_report_type
ON handle_report_content(report_type);

-- Index for finding reports by reporter (ba_id)
CREATE INDEX IF NOT EXISTS idx_handle_report_reporter
ON handle_report_content(ba_id);

-- Index for date range queries
CREATE INDEX IF NOT EXISTS idx_handle_report_created_at
ON handle_report_content(created_at);

-- Foreign key index (if not auto-created)
CREATE INDEX IF NOT EXISTS idx_handle_report_content_id
ON handle_report_content(report_content_id);

-- ========== MODERATION_ACTION INDEXES ==========

-- Index for finding actions by target user (most common query)
CREATE INDEX IF NOT EXISTS idx_moderation_action_user_id
ON moderation_action(user_id);

-- Index for filtering by action type (BAN_USER, WARN_USER, etc.)
CREATE INDEX IF NOT EXISTS idx_moderation_action_type
ON moderation_action(action_type);

-- Index for date range queries
CREATE INDEX IF NOT EXISTS idx_moderation_action_created_at
ON moderation_action(created_at);

-- Index for finding actions by admin (ba_id)
CREATE INDEX IF NOT EXISTS idx_moderation_action_admin
ON moderation_action(ba_id);

-- Composite index for user history queries (user_id + created_at)
CREATE INDEX IF NOT EXISTS idx_moderation_action_user_created_at
ON moderation_action(user_id, created_at);

-- ========== USER INDEXES (if not already present) ==========

-- Index for filtering locked users
CREATE INDEX IF NOT EXISTS idx_user_is_locked
ON users(is_locked) WHERE is_locked = true;

-- Index for filtering deleted users
CREATE INDEX IF NOT EXISTS idx_user_is_deleted
ON users(is_deleted) WHERE is_deleted = true;

-- Index for date range queries (user growth analytics)
CREATE INDEX IF NOT EXISTS idx_user_created_at
ON users(created_at);

-- ========== POST/COMMENT INDEXES (if not already present) ==========

-- Index for counting posts created today
CREATE INDEX IF NOT EXISTS idx_post_created_at
ON post(created_at);

-- Index for counting comments created today
CREATE INDEX IF NOT EXISTS idx_comment_created_at
ON comment(created_at);

-- Index for soft-deleted posts (if isDeleted field exists)
CREATE INDEX IF NOT EXISTS idx_post_is_deleted
ON post(is_deleted) WHERE is_deleted = true;

-- Index for soft-deleted comments (if isDeleted field exists)
CREATE INDEX IF NOT EXISTS idx_comment_is_deleted
ON comment(is_deleted) WHERE is_deleted = true;

-- =====================================================
-- VERIFICATION QUERIES
-- Run these after migration to verify indexes
-- =====================================================

-- Check index usage statistics (PostgreSQL)
-- SELECT schemaname, tablename, indexname, idx_scan
-- FROM pg_stat_user_indexes
-- WHERE tablename IN ('report_content', 'handle_report_content', 'moderation_action')
-- ORDER BY idx_scan DESC;

-- Check table statistics
-- SELECT relname, n_live_tup, n_dead_tup
-- FROM pg_stat_user_tables
-- WHERE relname IN ('report_content', 'handle_report_content', 'moderation_action');
