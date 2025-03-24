CREATE TABLE teams (
    team_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) UNIQUE NOT NULL,
    leader_id UUID NOT NULL,
    ranking VARCHAR(50) CHECK (ranking IN ('beginner', 'medium', 'advanced', 'world-class')),
    capacity INT NOT NULL CHECK (capacity > 0),  -- Team size
    preferred_start_time TIME NOT NULL, -- Start time preference (e.g., 18:00)
    preferred_end_time TIME NOT NULL,   -- End time preference (e.g., 21:00)
    preferred_venues JSONB DEFAULT '[]', -- Store venue UUIDs as JSON array
    FOREIGN KEY (leader_id) REFERENCES players(player_id) ON DELETE CASCADE
);

-- Add team_id column to the player table
ALTER TABLE players ADD COLUMN team_id UUID REFERENCES teams(team_id) ON DELETE SET NULL;
