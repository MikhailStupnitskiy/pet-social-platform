CREATE TABLE social_posts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    author_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    pet_id UUID NOT NULL REFERENCES pets(id) ON DELETE CASCADE,

    body TEXT NOT NULL,
    image_url TEXT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_social_posts_created_at
    ON social_posts(created_at DESC);

CREATE INDEX idx_social_posts_pet_id
    ON social_posts(pet_id);

CREATE INDEX idx_social_posts_author_user_id
    ON social_posts(author_user_id);
