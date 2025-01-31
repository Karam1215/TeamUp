CREATE TABLE player
(
    user_id         UUID NOT NULL,
    user_name       VARCHAR(255),
    first_name      VARCHAR(255),
    last_name       VARCHAR(255),
    email           VARCHAR(255),
    phone_number    VARCHAR(255),
    password        VARCHAR(255),
    birth_data      date,
    gender          VARCHAR(255),
    city            VARCHAR(255),
    profile_picture VARCHAR(255),
    created_at      TIMESTAMP WITHOUT TIME ZONE,
    bio             VARCHAR(255),
    sport           VARCHAR(255),
    CONSTRAINT pk_player PRIMARY KEY (user_id)
);