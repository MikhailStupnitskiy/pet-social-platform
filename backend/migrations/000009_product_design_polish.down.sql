DROP INDEX IF EXISTS idx_messages_chat_created_at;
DROP INDEX IF EXISTS idx_chat_read_states_user_id;
DROP TABLE IF EXISTS chat_read_states;

ALTER TABLE handler_profiles
    DROP COLUMN IF EXISTS longitude,
    DROP COLUMN IF EXISTS latitude,
    DROP COLUMN IF EXISTS avatar_url;

ALTER TABLE pets
    DROP COLUMN IF EXISTS longitude,
    DROP COLUMN IF EXISTS latitude,
    DROP COLUMN IF EXISTS search_radius_meters,
    DROP COLUMN IF EXISTS matching_goal,
    DROP COLUMN IF EXISTS health_notes,
    DROP COLUMN IF EXISTS interests,
    DROP COLUMN IF EXISTS personality_tags,
    DROP COLUMN IF EXISTS photo_url;
