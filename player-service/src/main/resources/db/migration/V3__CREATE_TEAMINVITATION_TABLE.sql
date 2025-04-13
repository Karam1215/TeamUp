CREATE TABLE team_invitations (
    invitation_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    team_id UUID NOT NULL,
    invited_player_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, ACCEPTED, DECLINED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (team_id) REFERENCES teams(team_id) ON DELETE CASCADE,
    FOREIGN KEY (invited_player_id) REFERENCES players(player_id) ON DELETE CASCADE,

    UNIQUE (team_id, invited_player_id)
);
