CREATE TABLE routine_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pet_id UUID NOT NULL REFERENCES pets(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    category TEXT NOT NULL,
    schedule_time TIME,
    notes TEXT,
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_routine_items_pet_id ON routine_items(pet_id);
CREATE INDEX idx_routine_items_category ON routine_items(category);

CREATE TABLE routine_completions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    routine_item_id UUID NOT NULL REFERENCES routine_items(id) ON DELETE CASCADE,
    completed_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_routine_completions_routine_item_id ON routine_completions(routine_item_id);
CREATE INDEX idx_routine_completions_completed_at ON routine_completions(completed_at);