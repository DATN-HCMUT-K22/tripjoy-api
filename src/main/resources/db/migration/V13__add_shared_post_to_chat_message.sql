-- Drop the existing shared_post_url column if it exists
ALTER TABLE chat_message DROP COLUMN IF EXISTS shared_post_url;

-- Add the new shared_post_id column
ALTER TABLE chat_message ADD COLUMN shared_post_id UUID;

-- Add foreign key constraint
ALTER TABLE chat_message ADD CONSTRAINT fk_chat_message_shared_post 
FOREIGN KEY (shared_post_id) REFERENCES post(id) ON DELETE SET NULL;
