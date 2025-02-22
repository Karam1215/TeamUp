CREATE TABLE IF NOT EXISTS team_players (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    team_id UUID NOT NULL,
    player_id UUID NOT NULL,
    joined_at TIMESTAMP DEFAULT NOW(),
    role VARCHAR(50) CHECK (role IN ('Leader', 'Member')),
    FOREIGN KEY (team_id) REFERENCES teams(team_id) ON DELETE CASCADE
);