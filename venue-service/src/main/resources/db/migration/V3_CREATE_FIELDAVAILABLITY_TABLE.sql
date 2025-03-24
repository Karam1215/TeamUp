CREATE TABLE field_availability (
    availability_id UUID PRIMARY KEY,
    field_id UUID REFERENCES fields(field_id),
    date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status VARCHAR(20) DEFAULT 'AVAILABLE',
    match_id UUID
);