ALTER TABLE users
    ADD COLUMN is_handler BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE handler_profiles (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    display_name TEXT NOT NULL,
    city TEXT,
    bio TEXT,
    experience_years INTEGER NOT NULL DEFAULT 0 CHECK (experience_years >= 0),
    conditions TEXT,
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    rating_avg NUMERIC(3,2) NOT NULL DEFAULT 0 CHECK (rating_avg >= 0 AND rating_avg <= 5),
    reviews_count INTEGER NOT NULL DEFAULT 0 CHECK (reviews_count >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_handler_profiles_city ON handler_profiles(city);
CREATE INDEX idx_handler_profiles_is_active ON handler_profiles(is_active);
CREATE INDEX idx_handler_profiles_rating_avg ON handler_profiles(rating_avg);

CREATE TABLE handler_services (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    handler_user_id UUID NOT NULL REFERENCES handler_profiles(user_id) ON DELETE CASCADE,
    service_type TEXT NOT NULL CHECK (service_type IN ('walking', 'sitting', 'training', 'grooming', 'other')),
    title TEXT NOT NULL,
    description TEXT,
    price_cents INTEGER NOT NULL CHECK (price_cents >= 0),
    duration_minutes INTEGER CHECK (duration_minutes IS NULL OR duration_minutes > 0),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_handler_services_handler_user_id ON handler_services(handler_user_id);
CREATE INDEX idx_handler_services_service_type ON handler_services(service_type);
CREATE INDEX idx_handler_services_is_active ON handler_services(is_active);

CREATE TABLE service_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    service_id UUID NOT NULL REFERENCES handler_services(id) ON DELETE RESTRICT,
    client_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    handler_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    pet_id UUID NOT NULL REFERENCES pets(id) ON DELETE CASCADE,
    requested_date DATE NOT NULL,
    requested_time TIME NOT NULL,
    comment TEXT,
    status TEXT NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'accepted', 'rejected', 'cancelled', 'completed')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT service_requests_no_self_request CHECK (client_user_id <> handler_user_id)
);

CREATE INDEX idx_service_requests_client_user_id ON service_requests(client_user_id);
CREATE INDEX idx_service_requests_handler_user_id ON service_requests(handler_user_id);
CREATE INDEX idx_service_requests_status ON service_requests(status);
CREATE INDEX idx_service_requests_service_id ON service_requests(service_id);

CREATE TABLE handler_reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    request_id UUID NOT NULL UNIQUE REFERENCES service_requests(id) ON DELETE CASCADE,
    handler_user_id UUID NOT NULL REFERENCES handler_profiles(user_id) ON DELETE CASCADE,
    client_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    body TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_handler_reviews_handler_user_id ON handler_reviews(handler_user_id);
CREATE INDEX idx_handler_reviews_client_user_id ON handler_reviews(client_user_id);
