DROP INDEX IF EXISTS idx_routine_items_repeat_rule;

ALTER TABLE routine_items
    DROP COLUMN IF EXISTS repeat_rule;
