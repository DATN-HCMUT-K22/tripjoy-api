-- V18: Optimize Itinerary performance (Index + Total Expense Denormalization)

-- 1. Add index for personal itinerary lookups
CREATE INDEX IF NOT EXISTS idx_itinerary_user_deleted ON itinerary (user_id, is_deleted);

-- 2. Denormalize total_expense in itinerary table
ALTER TABLE itinerary ADD COLUMN IF NOT EXISTS total_expense DECIMAL(19, 2) DEFAULT 0;

-- 3. Initialize data from existing expenses
UPDATE itinerary i
SET total_expense = (
    SELECT COALESCE(SUM(amount), 0)
    FROM expense e
    WHERE e.itinerary_id = i.id
);
