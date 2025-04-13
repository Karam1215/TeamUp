CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE field_availability (
    booking_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    field_id UUID NOT NULL,
    match_id UUID not null,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (field_id) REFERENCES fields(field_id) ON DELETE CASCADE,

    EXCLUDE USING gist (
        field_id WITH =,
        tsrange(start_time, end_time) WITH &&
    )
);

CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_updated_at
BEFORE UPDATE ON field_availability
FOR EACH ROW
EXECUTE FUNCTION update_updated_at();