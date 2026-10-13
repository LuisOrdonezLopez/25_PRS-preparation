CREATE TABLE IF NOT EXISTS preparation (
    id                   BIGSERIAL       PRIMARY KEY,
    request_id           BIGINT          NOT NULL,
    type                 VARCHAR(20)     NOT NULL DEFAULT 'TITULAR',
    session_date         DATE            NOT NULL,
    status               VARCHAR(30)     NOT NULL DEFAULT 'PENDING',
    attendance_status    VARCHAR(20),
    payment_amount       DECIMAL(10,2),
    payment_status       VARCHAR(20),
    payment_date         DATE,
    payment_receipt_url  TEXT,
    exam_score           INTEGER,
    exam_passed          BOOLEAN,
    notes                TEXT,
    created_at           TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP       NOT NULL DEFAULT NOW(),
    tenant_id            VARCHAR(100)    NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_prep_request  ON preparation(request_id);
CREATE INDEX IF NOT EXISTS idx_prep_status   ON preparation(status);
CREATE INDEX IF NOT EXISTS idx_prep_req_stat ON preparation(request_id, status);
CREATE INDEX IF NOT EXISTS idx_prep_tenant   ON preparation(tenant_id);
