DROP TABLE matches;

CREATE TABLE matches (
    match_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    team1_id UUID NOT NULL,
    team2_id UUID NOT NULL,
    scheduled_time TIMESTAMP,
    venue_id UUID,              -- Changed from 'venue' to 'venue_id'
    field_id UUID,              -- New column
    booking_id UUID,            -- New column
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);