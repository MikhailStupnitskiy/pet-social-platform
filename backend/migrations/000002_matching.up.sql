CREATE TABLE swipe_actions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_pet_id UUID NOT NULL REFERENCES pets(id) ON DELETE CASCADE,
    target_pet_id UUID NOT NULL REFERENCES pets(id) ON DELETE CASCADE,
    action TEXT NOT NULL CHECK (action IN ('like', 'pass')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT swipe_unique_pair UNIQUE (source_pet_id, target_pet_id)
);

CREATE INDEX idx_swipe_actions_source_pet_id ON swipe_actions(source_pet_id);
CREATE INDEX idx_swipe_actions_target_pet_id ON swipe_actions(target_pet_id);
CREATE INDEX idx_swipe_actions_action ON swipe_actions(action);

CREATE TABLE matches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pet1_id UUID NOT NULL REFERENCES pets(id) ON DELETE CASCADE,
    pet2_id UUID NOT NULL REFERENCES pets(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT matches_pet_order CHECK (pet1_id <> pet2_id),
    CONSTRAINT matches_unique_pair UNIQUE (pet1_id, pet2_id)
);

CREATE INDEX idx_matches_pet1_id ON matches(pet1_id);
CREATE INDEX idx_matches_pet2_id ON matches(pet2_id);