ALTER TABLE event
    ADD COLUMN registration_fields TEXT[] NOT NULL DEFAULT '{}',
    ADD COLUMN registration_slots INT NOT NULL DEFAULT 0,
    ADD COLUMN registration_slots_waiting INT NOT NULL DEFAULT 0,
    ADD COLUMN registration_open BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN registrations TEXT[] NOT NULL DEFAULT '{}';