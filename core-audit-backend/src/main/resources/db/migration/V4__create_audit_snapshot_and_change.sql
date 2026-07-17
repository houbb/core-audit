-- ============================================================
-- V4: Create diff tables for P3 Diff Runtime
-- Description: Store complete snapshots and structured field-level
--              changes for every UPDATE audit event.
-- ============================================================

-- ============================================================
-- audit_snapshot: 完整对象快照
-- ============================================================
CREATE TABLE IF NOT EXISTS audit_snapshot (
    id              TEXT        PRIMARY KEY NOT NULL,
    audit_id        TEXT        NOT NULL,                           -- 关联 audit_event
    snapshot_type   TEXT        NOT NULL,                           -- BEFORE / AFTER
    content_json    TEXT        NOT NULL,                           -- 完整对象 JSON
    created_at      TEXT        NOT NULL,                           -- 快照创建时间（ISO-8601）
    create_time     TEXT        NOT NULL DEFAULT (datetime('now','localtime')),
    update_time     TEXT        NOT NULL DEFAULT (datetime('now','localtime')),
    create_user     TEXT        DEFAULT 'system',
    update_user     TEXT        DEFAULT 'system',
    FOREIGN KEY (audit_id) REFERENCES audit_event(id)
);

CREATE INDEX IF NOT EXISTS idx_snapshot_audit_id
    ON audit_snapshot(audit_id);

CREATE INDEX IF NOT EXISTS idx_snapshot_type
    ON audit_snapshot(snapshot_type);

-- ============================================================
-- audit_change: 结构化字段级变更记录
-- ============================================================
CREATE TABLE IF NOT EXISTS audit_change (
    id              TEXT        PRIMARY KEY NOT NULL,
    audit_id        TEXT        NOT NULL,                           -- 关联 audit_event
    field_name      TEXT        NOT NULL,                           -- 字段名
    change_type     TEXT        NOT NULL,                           -- ADD / REMOVE / UPDATE / UNCHANGED
    before_value    TEXT,                                           -- 变更前值
    after_value     TEXT,                                           -- 变更后值
    create_time     TEXT        NOT NULL DEFAULT (datetime('now','localtime')),
    update_time     TEXT        NOT NULL DEFAULT (datetime('now','localtime')),
    create_user     TEXT        DEFAULT 'system',
    update_user     TEXT        DEFAULT 'system',
    FOREIGN KEY (audit_id) REFERENCES audit_event(id)
);

CREATE INDEX IF NOT EXISTS idx_change_audit_id
    ON audit_change(audit_id);

CREATE INDEX IF NOT EXISTS idx_change_field_name
    ON audit_change(field_name);

CREATE INDEX IF NOT EXISTS idx_change_type
    ON audit_change(change_type);
