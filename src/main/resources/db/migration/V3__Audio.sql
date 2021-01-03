UPDATE feature SET native_feature = 'AUDIO' WHERE native_feature = 'PODCASTS';

CREATE TABLE audio (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    name_de TEXT NOT NULL,
    image TEXT,
    image_de TEXT,
    position INT NOT NULL
);

CREATE TABLE audio_file (
    id SERIAL PRIMARY KEY,
    audio_id INT NOT NULL REFERENCES audio(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    name_de TEXT NOT NULL,
    file TEXT NOT NULL,
    file_de TEXT NOT NULL,
    text TEXT,
    text_de TEXT,
    position INT NOT NULL
);