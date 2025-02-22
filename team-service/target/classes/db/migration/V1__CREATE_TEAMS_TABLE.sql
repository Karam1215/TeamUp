CREATE TABLE IF NOT EXISTS teams (
    team_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    team_name VARCHAR(100) NOT NULL UNIQUE,
    leader_id UUID NOT NULL,
    sport_type VARCHAR(50) NOT NULL ,
    max_players INT NOT NULL CHECK ( max_players>= 1),
    current_players INT DEFAULT 1 CHECK (current_players >= 1),
    skill_level VARCHAR(50) CHECK (skill_level IN ('Beginner', 'Intermediate', 'Advanced')),
    location VARCHAR(255) NOT NULL,
    latitude DECIMAL(9,6) NOT NULL,
    longitude DECIMAL(9,6) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

