
CREATE TABLE matches (
    match_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    team1_id UUID NOT NULL,
    team2_id UUID NOT NULL,
    scheduled_time TIMESTAMP,
    venues_id JSONB DEFAULT '[]',
    status VARCHAR(20) DEFAULT 'matched' CHECK (status IN ('matched', 'expired', 'canceled')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);