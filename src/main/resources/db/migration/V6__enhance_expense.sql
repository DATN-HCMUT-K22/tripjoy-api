-- V6: Enhance expense table
-- 1. receipt_image_urls: store comma-separated image URLs (evidence photos of receipts)
-- 2. paid_by: FK to users - who actually paid (may differ from who created the expense)
-- 3. paid_at: timestamp when the payment was actually made
-- 4. trip_item_id: optional FK to trip_item (nullable - not all expenses belong to a specific location)

ALTER TABLE expense ADD COLUMN receipt_image_urls TEXT;

ALTER TABLE expense ADD COLUMN paid_by UUID REFERENCES users(id) ON DELETE SET NULL;

ALTER TABLE expense ADD COLUMN paid_at TIMESTAMP;

ALTER TABLE expense ADD COLUMN trip_item_id UUID REFERENCES trip_item(id) ON DELETE SET NULL;

-- Index for filtering/grouping expenses by payer within an itinerary (used by summary API)
CREATE INDEX idx_expense_itinerary_paid_by ON expense(itinerary_id, paid_by);

-- Index for looking up expenses linked to a specific trip item
CREATE INDEX idx_expense_trip_item_id ON expense(trip_item_id);
