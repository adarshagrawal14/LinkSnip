CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

ALTER TABLE urls ADD CONSTRAINT fk_urls_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;
