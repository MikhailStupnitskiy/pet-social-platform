DROP INDEX IF EXISTS idx_chats_handler_user_id;
DROP INDEX IF EXISTS idx_chats_client_user_id;
DROP INDEX IF EXISTS idx_chats_service_request_id;

ALTER TABLE chats
    DROP CONSTRAINT IF EXISTS chats_source_check,
    DROP COLUMN IF EXISTS handler_user_id,
    DROP COLUMN IF EXISTS client_user_id,
    DROP COLUMN IF EXISTS service_request_id,
    ALTER COLUMN pet2_id SET NOT NULL,
    ALTER COLUMN match_id SET NOT NULL;
