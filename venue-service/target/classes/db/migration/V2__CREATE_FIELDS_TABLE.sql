CREATE TABLE fields (
    field_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    venue_id UUID REFERENCES venues(venue_id),
    name VARCHAR(255) NOT NULL,
    price_per_hour DECIMAL(10, 2) NOT NULL,
    capacity INT NOT NULL,
    created_at TIMESTAMP
);