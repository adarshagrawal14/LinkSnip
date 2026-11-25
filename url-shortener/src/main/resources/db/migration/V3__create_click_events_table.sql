CREATE TABLE click_events (
    id          BIGSERIAL PRIMARY KEY,
    url_id      BIGINT NOT NULL REFERENCES urls(id) ON DELETE CASCADE,
    clicked_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    referrer    VARCHAR(512) NULL,
    user_agent  VARCHAR(512) NULL,
    ip_hash     VARCHAR(64)  NULL
);

CREATE INDEX idx_click_events_url_id_clicked_at ON click_events (url_id, clicked_at);
