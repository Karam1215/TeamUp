CREATE TABLE match_requests (
    request_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    team_id UUID NOT NULL,
    ranking VARCHAR(50) CHECK (ranking IN ('beginner', 'medium', 'advanced', 'world-class')),
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    team_size INT CHECK (team_size >= 1),
    preferred_venues JSONB DEFAULT '[]',
    day date not null ,
    status VARCHAR(20) DEFAULT 'pending' CHECK (status IN ('pending', 'matched', 'expired')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);