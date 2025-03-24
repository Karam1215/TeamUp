CREATE TABLE IF NOT EXISTS venues(
    venue_id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    address VARCHAR(255),
    region VARCHAR(100),
    latitude DECIMAL(9,6),
    longitude DECIMAL(9,6),
    description TEXT,
    phone_number VARCHAR(20),
    email VARCHAR(100),
    opening_time TIME,
    closing_time TIME,
    created_at TIMESTAMP
)