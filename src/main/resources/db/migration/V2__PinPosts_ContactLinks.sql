ALTER TABLE post ALTER COLUMN channel_id DROP NOT NULL;
ALTER TABLE post ADD COLUMN pinned BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE post ALTER COLUMN pinned DROP DEFAULT;

ALTER TABLE contact ADD COLUMN links TEXT[] NOT NULL DEFAULT '{}';
ALTER TABLE contact ALTER COLUMN links DROP DEFAULT;