CREATE TABLE matches (
    match_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    team1_id UUID NOT NULL,
    team2_id UUID NOT NULL,
    scheduled_time TIMESTAMP,
    venue UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
