create table email_verification_tokens(
    token_id BIGSERIAL PRIMARY KEY,
    token VARCHAR UNIQUE NOT NULL ,
    created_at TIMESTAMP NOT NULL ,
    expires_at TIMESTAMP  NOT NULL,
    confirmed_at TIMESTAMP,
    player_id UUID NOT NULL REFERENCES players(player_id) ON DELETE CASCADE
);