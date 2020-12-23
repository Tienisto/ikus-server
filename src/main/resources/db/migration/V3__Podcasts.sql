CREATE TABLE podcast (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    image TEXT,
    position INT NOT NULL
);

CREATE TABLE podcast_file (
    id SERIAL PRIMARY KEY,
    podcast_id INT NOT NULL REFERENCES podcast(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    file TEXT NOT NULL,
    text TEXT,
    position INT NOT NULL
);