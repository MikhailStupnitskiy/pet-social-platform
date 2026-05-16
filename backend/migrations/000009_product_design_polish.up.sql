ALTER TABLE pets
    ADD COLUMN photo_url TEXT,
    ADD COLUMN personality_tags TEXT[] NOT NULL DEFAULT '{}',
    ADD COLUMN interests TEXT[] NOT NULL DEFAULT '{}',
    ADD COLUMN health_notes TEXT,
    ADD COLUMN matching_goal TEXT,
    ADD COLUMN search_radius_meters INTEGER NOT NULL DEFAULT 3000 CHECK (search_radius_meters > 0),
    ADD COLUMN latitude NUMERIC(9,6),
    ADD COLUMN longitude NUMERIC(9,6);

ALTER TABLE handler_profiles
    ADD COLUMN avatar_url TEXT,
    ADD COLUMN latitude NUMERIC(9,6),
    ADD COLUMN longitude NUMERIC(9,6);

CREATE TABLE chat_read_states (
    chat_id UUID NOT NULL REFERENCES chats(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    last_read_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (chat_id, user_id)
);

CREATE INDEX idx_chat_read_states_user_id ON chat_read_states(user_id);
CREATE INDEX idx_messages_chat_created_at ON messages(chat_id, created_at);
