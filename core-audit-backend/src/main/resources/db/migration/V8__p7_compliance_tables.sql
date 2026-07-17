-- ============================================================
-- V8: P7 Compliance Runtime — 企业合规表
-- Description: Create audit_retention_policy, audit_signature,
--              audit_legal_hold, audit_export_task tables
--              for enterprise compliance capabilities
-- ============================================================

-- ============================================================
-- audit_retention_policy: 保留策略表
-- 按 Module + Action 定义审计数据保留天数
-- ============================================================
CREATE TABLE IF NOT EXISTS audit_retention_policy (
    id              TEXT        PRIMARY KEY NOT NULL,
    module          TEXT        NOT NULL,                           -- AuditModule 枚举名称
    action          TEXT,                                           -- AuditAction 枚举名称（NULL = 该模块所有 action）
    retention_days  INTEGER     NOT NULL DEFAULT 365,               -- 保留天数
    archive         INTEGER     NOT NULL DEFAULT 0,                 -- 是否归档（0/1）
    enabled         INTEGER     NOT NULL DEFAULT 1,                 -- 是否启用（0/1）
    create_time     TEXT        NOT NULL DEFAULT (datetime('now','localtime')),
    update_time     TEXT        NOT NULL DEFAULT (datetime('now','localtime')),
    create_user     TEXT        DEFAULT 'system',
    update_user     TEXT        DEFAULT 'system'
);

CREATE INDEX IF NOT EXISTS idx_retention_module_action
    ON audit_retention_policy(module, action);

CREATE INDEX IF NOT EXISTS idx_retention_enabled
    ON audit_retention_policy(enabled);

-- ============================================================
-- audit_signature: 完整性签名表
-- 每条 Audit Event 的 SHA-256 哈希 + 链式 previous_hash
-- ============================================================
CREATE TABLE IF NOT EXISTS audit_signature (
    id              TEXT        PRIMARY KEY NOT NULL,
    audit_id        TEXT        NOT NULL UNIQUE,                    -- FK → audit_event.id，一条 Audit 一个签名
    hash            TEXT        NOT NULL,                           -- SHA-256 哈希值（hex 编码）
    previous_hash   TEXT        NOT NULL,                           -- 前一条 Audit 的 hash（GENESIS = 链起点）
    algorithm       TEXT        NOT NULL DEFAULT 'SHA-256',         -- 哈希算法
    created_at      TEXT        NOT NULL,                           -- 签名生成时间（ISO-8601）
    create_time     TEXT        NOT NULL DEFAULT (datetime('now','localtime')),
    update_time     TEXT        NOT NULL DEFAULT (datetime('now','localtime')),
    create_user     TEXT        DEFAULT 'system',
    update_user     TEXT        DEFAULT 'system',
    FOREIGN KEY (audit_id) REFERENCES audit_event(id)
);

CREATE INDEX IF NOT EXISTS idx_signature_audit
    ON audit_signature(audit_id);

CREATE INDEX IF NOT EXISTS idx_signature_created
    ON audit_signature(created_at);

-- ============================================================
-- audit_legal_hold: 法律保留表
-- 法律保留期间即使 Retention 到期也不能删除
-- ============================================================
CREATE TABLE IF NOT EXISTS audit_legal_hold (
    id              TEXT        PRIMARY KEY NOT NULL,
    audit_id        TEXT        NOT NULL,                           -- FK → audit_event.id
    reason          TEXT        NOT NULL,                           -- 法律保留原因（如 "案件 #2026-001"）
    owner           TEXT,                                           -- 负责人
    expired_at      TEXT,                                           -- 保留结束时间（ISO-8601，NULL = 永久）
    create_time     TEXT        NOT NULL DEFAULT (datetime('now','localtime')),
    update_time     TEXT        NOT NULL DEFAULT (datetime('now','localtime')),
    create_user     TEXT        DEFAULT 'system',
    update_user     TEXT        DEFAULT 'system',
    FOREIGN KEY (audit_id) REFERENCES audit_event(id)
);

CREATE INDEX IF NOT EXISTS idx_legal_hold_audit
    ON audit_legal_hold(audit_id);

CREATE INDEX IF NOT EXISTS idx_legal_hold_expire
    ON audit_legal_hold(expired_at);

-- ============================================================
-- audit_export_task: 合规导出任务表
-- 异步导出审计数据，支持 CSV / Excel / PDF
-- ============================================================
CREATE TABLE IF NOT EXISTS audit_export_task (
    id              TEXT        PRIMARY KEY NOT NULL,
    query_json      TEXT,                                           -- 查询条件 JSON（AuditQuery 序列化）
    format          TEXT        NOT NULL DEFAULT 'CSV',             -- ExportFormat: CSV / EXCEL / PDF
    mask_enabled    INTEGER     NOT NULL DEFAULT 1,                 -- 是否脱敏（0/1）
    include_diff    INTEGER     NOT NULL DEFAULT 0,                 -- 是否包含 Diff（0/1）
    include_timeline INTEGER    NOT NULL DEFAULT 0,                 -- 是否包含 Timeline（0/1）
    status          TEXT        NOT NULL DEFAULT 'PENDING',         -- ExportStatus: PENDING / PROCESSING / COMPLETED / FAILED
    file_path       TEXT,                                           -- 导出文件路径
    error_message   TEXT,                                           -- 错误信息
    created_by      TEXT,                                           -- 创建人
    created_at      TEXT        NOT NULL,                           -- 创建时间（ISO-8601）
    completed_at    TEXT,                                           -- 完成时间（ISO-8601）
    create_time     TEXT        NOT NULL DEFAULT (datetime('now','localtime')),
    update_time     TEXT        NOT NULL DEFAULT (datetime('now','localtime')),
    create_user     TEXT        DEFAULT 'system',
    update_user     TEXT        DEFAULT 'system'
);

CREATE INDEX IF NOT EXISTS idx_export_status_created
    ON audit_export_task(status, created_at);

CREATE INDEX IF NOT EXISTS idx_export_created_by
    ON audit_export_task(created_by);
