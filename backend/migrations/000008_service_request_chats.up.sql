ALTER TABLE chats
    ALTER COLUMN match_id DROP NOT NULL,
    ALTER COLUMN pet2_id DROP NOT NULL,
    ADD COLUMN service_request_id UUID UNIQUE REFERENCES service_requests(id) ON DELETE CASCADE,
    ADD COLUMN client_user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    ADD COLUMN handler_user_id UUID REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE chats
    ADD CONSTRAINT chats_source_check CHECK (
        (match_id IS NOT NULL AND service_request_id IS NULL AND pet2_id IS NOT NULL)
        OR
        (match_id IS NULL AND service_request_id IS NOT NULL AND client_user_id IS NOT NULL AND handler_user_id IS NOT NULL)
    );

CREATE INDEX idx_chats_service_request_id ON chats(service_request_id);
CREATE INDEX idx_chats_client_user_id ON chats(client_user_id);
CREATE INDEX idx_chats_handler_user_id ON chats(handler_user_id);
