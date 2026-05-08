-- V17: Add denormalized itinerary count to groups table for performance optimization
ALTER TABLE "groups" ADD COLUMN iti_count INTEGER DEFAULT 0;

-- Initialize iti_count for existing data
UPDATE "groups" g
SET iti_count = (
    SELECT COUNT(*) 
    FROM itinerary i 
    WHERE i.group_id = g.id 
      AND i.is_deleted = false
);
