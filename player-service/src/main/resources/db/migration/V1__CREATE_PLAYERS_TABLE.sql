CREATE TABLE IF NOT EXISTS players (
    player_id UUID PRIMARY KEY,
    user_name VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    email VARCHAR(255) NOT NULL UNIQUE, -- Only one UNIQUE constraint needed
    phone_number VARCHAR(15),
    date_of_birth DATE,
    gender VARCHAR(10),
    city VARCHAR(255) ,
    profile_picture VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    bio TEXT,
    sport VARCHAR(50)
    --reviews INTEGER DEFAULT 0
);

