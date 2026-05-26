-- V7: Add hide_expense flag to post
-- Allows post creators to share itinerary information publicly
-- while keeping expense details private (not accessible to non-members).
--
-- Design decision: independent boolean instead of a new PostVisibility enum value
-- so that visibility (who sees the post) and data redaction (what data is exposed)
-- remain separate, orthogonal concerns — easier to extend in the future.

ALTER TABLE post
    ADD COLUMN hide_expense BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN post.hide_expense IS
    'When TRUE, expense data of the linked itinerary is hidden from non-members '
    'even if the post itself is PUBLIC.';
