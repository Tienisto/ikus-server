CREATE TABLE ikus_user (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    password TEXT NOT NULL,
    access_posts BOOLEAN NOT NULL,
    access_events BOOLEAN NOT NULL,
    access_links BOOLEAN NOT NULL,
    access_hand_book BOOLEAN NOT NULL
);