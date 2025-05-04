CREATE TABLE matches (
    match_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    creator_team_id UUID NOT NULL,
    joined_team_id UUID NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    day date not null ,
    venues_id JSONB DEFAULT '[]',
    status VARCHAR(20) DEFAULT 'matched' CHECK (status IN ('matched', 'expired', 'canceled')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);