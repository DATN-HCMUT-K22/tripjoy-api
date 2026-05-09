-- V12: Add index for itinerary user lookups to resolve load test timeouts
CREATE INDEX IF NOT EXISTS idx_itinerary_user_deleted ON itinerary (user_id, is_deleted);
CREATE INDEX IF NOT EXISTS idx_expense_itinerary_id ON expense (itinerary_id);
CREATE INDEX IF NOT EXISTS idx_trip_item_itinerary_id ON trip_item (itinerary_id);
