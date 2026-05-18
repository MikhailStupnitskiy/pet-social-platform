ALTER TABLE routine_items
    ADD COLUMN repeat_rule TEXT NOT NULL DEFAULT 'none'
    CHECK (repeat_rule IN ('none', 'daily', 'weekly', 'monthly'));

CREATE INDEX idx_routine_items_repeat_rule ON routine_items(repeat_rule);
