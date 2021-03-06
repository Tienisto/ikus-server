ALTER TABLE post ADD COLUMN pinned BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE post ALTER COLUMN pinned DROP DEFAULT;

ALTER TABLE contact ADD COLUMN links TEXT[] NOT NULL DEFAULT '{}';
ALTER TABLE contact ALTER COLUMN links DROP DEFAULT;

CREATE TABLE feature (
    id SERIAL PRIMARY KEY,
    position INT NOT NULL,
    favorite BOOLEAN NOT NULL,
    name TEXT,
    name_de TEXT,
    icon TEXT,
    native_feature TEXT,
    post_id INT REFERENCES post(id) ON DELETE CASCADE,
    link_id INT REFERENCES link(id) ON DELETE CASCADE
);