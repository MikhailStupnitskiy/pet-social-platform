DROP INDEX IF EXISTS idx_matching_events_event_type;
DROP INDEX IF EXISTS idx_matching_events_target_pet_id;
DROP INDEX IF EXISTS idx_matching_events_source_pet_id;
DROP INDEX IF EXISTS idx_pets_location;
DROP INDEX IF EXISTS idx_pets_matching_search;

DROP TABLE IF EXISTS matching_events;

ALTER TABLE pets
    DROP CONSTRAINT IF EXISTS pets_matching_goal_check;
