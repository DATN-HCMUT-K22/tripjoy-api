-- V19: Optimize Post performance (Denormalization + Indexing)

-- 1. Add denormalized columns
ALTER TABLE post ADD COLUMN IF NOT EXISTS like_count BIGINT DEFAULT 0;
ALTER TABLE post ADD COLUMN IF NOT EXISTS comment_count BIGINT DEFAULT 0;

-- 2. Initialize data
UPDATE post p
SET like_count = (SELECT COUNT(*) FROM like_post lp WHERE lp.post_id = p.id),
    comment_count = (SELECT COUNT(*) FROM comment c WHERE c.post_id = p.id AND c.is_deleted = false);

-- 3. Add index for feed and search
CREATE INDEX IF NOT EXISTS idx_post_creator_deleted ON post (creator_id, is_deleted);
CREATE INDEX IF NOT EXISTS idx_post_created_at ON post (created_at DESC);
