CREATE TABLE IF NOT EXISTS venues(
    venue_id UUID PRIMARY KEY,
    name VARCHAR NOT NULL UNIQUE,
    address VARCHAR,
    region VARCHAR(100),
    latitude DECIMAL,
    longitude DECIMAL,
    description TEXT,
    phone_number VARCHAR,
    email VARCHAR(100),
    opening_time TIME,
    closing_time TIME,
    created_at TIMESTAMP
)