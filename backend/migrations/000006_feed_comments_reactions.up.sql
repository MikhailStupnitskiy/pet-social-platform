CREATE TABLE social_post_comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id UUID NOT NULL REFERENCES social_posts(id) ON DELETE CASCADE,
    author_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    body TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_social_post_comments_post_id_created_at
    ON social_post_comments(post_id, created_at ASC);

CREATE INDEX idx_social_post_comments_author_user_id
    ON social_post_comments(author_user_id);

CREATE TABLE social_post_reactions (
    post_id UUID NOT NULL REFERENCES social_posts(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    reaction_type TEXT NOT NULL CHECK (reaction_type IN ('like', 'love', 'funny', 'support')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT social_post_reactions_unique_user UNIQUE (post_id, user_id)
);

CREATE INDEX idx_social_post_reactions_post_id
    ON social_post_reactions(post_id);

CREATE INDEX idx_social_post_reactions_user_id
    ON social_post_reactions(user_id);
