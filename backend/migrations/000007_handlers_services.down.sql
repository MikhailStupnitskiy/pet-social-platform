DROP TABLE IF EXISTS handler_reviews;
DROP TABLE IF EXISTS service_requests;
DROP TABLE IF EXISTS handler_services;
DROP TABLE IF EXISTS handler_profiles;

ALTER TABLE users
    DROP COLUMN IF EXISTS is_handler;
