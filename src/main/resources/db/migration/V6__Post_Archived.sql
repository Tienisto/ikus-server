ALTER TABLE post
    ADD COLUMN archived BOOLEAN NOT NULL DEFAULT FALSE,
    DROP COLUMN pinned;
ALTER TABLE post ALTER COLUMN archived DROP DEFAULT;