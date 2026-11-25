CREATE SEQUENCE urls_id_seq START WITH 1000;

CREATE TABLE urls (
    id              BIGINT PRIMARY KEY,
    short_code      VARCHAR(20)  NOT NULL,
    long_url        VARCHAR(2048) NOT NULL,
    user_id         BIGINT       NULL,
    is_custom_alias BOOLEAN      NOT NULL DEFAULT FALSE,
    click_count     BIGINT       NOT NULL DEFAULT 0,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    expires_at      TIMESTAMPTZ  NULL,
    deleted_at      TIMESTAMPTZ  NULL
);

ALTER SEQUENCE urls_id_seq OWNED BY urls.id;

CREATE UNIQUE INDEX idx_urls_short_code ON urls (short_code);
CREATE INDEX idx_urls_user_id ON urls (user_id);
CREATE INDEX idx_urls_expires_at ON urls (expires_at) WHERE expires_at IS NOT NULL;
