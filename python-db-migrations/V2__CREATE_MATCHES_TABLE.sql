
CREATE TABLE matches (
    match_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    team1_id UUID NOT NULL,
    team2_id UUID NOT NULL,
    scheduled_time TIMESTAMP,
    venue_id UUID,
    field_id UUID,
    booking_id UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);