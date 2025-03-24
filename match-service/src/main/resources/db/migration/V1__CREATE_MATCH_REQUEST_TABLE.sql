CREATE TABLE match_requests (
    request_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    team_id UUID NOT NULL UNIQUE,
    ranking VARCHAR(50) CHECK (ranking IN ('beginner', 'medium', 'advanced', 'world-class')),
    preferred_start_time TIME NOT NULL,
    preferred_end_time TIME NOT NULL,
    preferred_venues JSONB DEFAULT '[]',
    status VARCHAR(20) DEFAULT 'pending' CHECK (status IN ('pending', 'matched', 'expired')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

