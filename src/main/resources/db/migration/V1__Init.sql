CREATE TABLE ikus_user (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    password TEXT NOT NULL
);

CREATE TABLE channel (
    id SERIAL PRIMARY KEY,
    type TEXT NOT NULL,
    name TEXT NOT NULL,
    name_de TEXT NOT NULL
);

CREATE TABLE post (
    id SERIAL PRIMARY KEY,
    type TEXT NOT NULL,
    channel_id INT NOT NULL REFERENCES channel(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    title TEXT NOT NULL,
    title_de TEXT NOT NULL,
    content TEXT NOT NULL,
    content_de TEXT NOT NULL
);

CREATE TABLE post_file (
    id SERIAL PRIMARY KEY,
    post_id INT REFERENCES post(id) ON DELETE SET NULL,
    file_name TEXT NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE event (
    id SERIAL PRIMARY KEY,
    channel_id INT NOT NULL REFERENCES channel(id) ON DELETE CASCADE,
    place TEXT,
    coords POINT,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE,
    name TEXT NOT NULL,
    name_de TEXT NOT NULL,
    info TEXT,
    info_de TEXT
);

CREATE TABLE link_group (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    name_de TEXT NOT NULL
);

CREATE TABLE link (
    id SERIAL PRIMARY KEY,
    group_id INT NOT NULL REFERENCES link_group(id) ON DELETE CASCADE,
    url TEXT NOT NULL,
    url_de TEXT NOT NULL,
    info TEXT NOT NULL,
    info_de TEXT NOT NULL
);

CREATE TABLE handbook_bookmark (
    id SERIAL PRIMARY KEY,
    locale TEXT NOT NULL,
    name TEXT NOT NULL,
    page INT NOT NULL
);

CREATE TABLE contact (
    id SERIAL PRIMARY KEY,
    email TEXT,
    phoneNumber TEXT,
    place TEXT,
    name TEXT NOT NULL,
    name_de TEXT NOT NULL,
    opening_hours TEXT,
    opening_hours_de TEXT
);

CREATE TABLE log (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES ikus_user(id) ON DELETE SET NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    type TEXT NOT NULL,
    info TEXT NOT NULL
);

CREATE TABLE app_start_monthly (
    date DATE PRIMARY KEY, -- first day of month
    android INT NOT NULL,
    ios INT NOT NULL
);

CREATE TABLE app_start_weekly (
    date DATE PRIMARY KEY, -- mondays
    android INT NOT NULL,
    ios INT NOT NULL
);

CREATE TABLE app_start_cache (
    device_id TEXT PRIMARY KEY,
    platform TEXT NOT NULL,
    last_login TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE config (
    key TEXT PRIMARY KEY,
    value TEXT
);