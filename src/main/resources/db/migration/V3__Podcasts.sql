CREATE TABLE podcast (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    name_de TEXT NOT NULL,
    image TEXT,
    image_de TEXT,
    position INT NOT NULL
);

CREATE TABLE podcast_file (
    id SERIAL PRIMARY KEY,
    podcast_id INT NOT NULL REFERENCES podcast(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    name_de TEXT NOT NULL,
    file TEXT NOT NULL,
    file_de TEXT NOT NULL,
    text TEXT,
    text_de TEXT,
    position INT NOT NULL
);