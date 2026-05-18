UPDATE pets
SET matching_goal = CASE
    WHEN lower(trim(matching_goal)) IN ('breeding', 'breed', 'разведение') THEN 'breeding'
    WHEN lower(trim(matching_goal)) IN ('walk', 'walking', 'прогулка', 'прогулки') THEN 'walk'
    ELSE NULL
END
WHERE matching_goal IS NOT NULL;

ALTER TABLE pets
    ADD CONSTRAINT pets_matching_goal_check
    CHECK (matching_goal IS NULL OR matching_goal IN ('walk', 'breeding'));

CREATE TABLE matching_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_pet_id UUID NOT NULL REFERENCES pets(id) ON DELETE CASCADE,
    target_pet_id UUID NOT NULL REFERENCES pets(id) ON DELETE CASCADE,
    event_type TEXT NOT NULL CHECK (event_type IN ('profile_open')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_pets_matching_search ON pets (is_active, species, matching_goal, breed, sex);
CREATE INDEX idx_pets_location ON pets (latitude, longitude);
CREATE INDEX idx_matching_events_source_pet_id ON matching_events(source_pet_id);
CREATE INDEX idx_matching_events_target_pet_id ON matching_events(target_pet_id);
CREATE INDEX idx_matching_events_event_type ON matching_events(event_type);
