CREATE TABLE audit_logs (
    id           BIGSERIAL PRIMARY KEY,
    actor_id     BIGINT        REFERENCES users(id) ON DELETE SET NULL,
    actor_name   VARCHAR(100)  NOT NULL,
    actor_email  VARCHAR(150)  NOT NULL,
    action       VARCHAR(40)   NOT NULL,
    target_type  VARCHAR(20),                 -- USER | EMPLOYEE
    target_id    BIGINT,
    target_name  VARCHAR(200),
    details      VARCHAR(500),
    created_at   TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at DESC);

ALTER TABLE users ADD COLUMN notifications_seen_at TIMESTAMP;
