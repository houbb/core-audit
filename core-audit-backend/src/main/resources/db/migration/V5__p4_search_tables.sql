-- ============================================================
-- V5: P4 Search Runtime — 保存查询 + 搜索历史 + 性能索引
-- Description: Create audit_saved_query, audit_search_history
--              tables and composite performance indexes
-- ============================================================

-- ============================================================
-- audit_saved_query: 保存的查询条件
-- ============================================================
CREATE TABLE IF NOT EXISTS audit_saved_query (
    id              TEXT        PRIMARY KEY NOT NULL,
    name            TEXT        NOT NULL,                           -- 查询名称
    owner_id        TEXT,                                           -- 创建人 ID
    query_json      TEXT        NOT NULL,                           -- AuditQuery DSL 序列化为 JSON
    is_public       INTEGER     DEFAULT 0,                          -- 0=私有, 1=团队共享
    created_at      TEXT        NOT NULL,                           -- 创建时间（ISO-8601）
    create_time     TEXT        NOT NULL DEFAULT (datetime('now','localtime')),
    update_time     TEXT        NOT NULL DEFAULT (datetime('now','localtime')),
    create_user     TEXT        DEFAULT 'system',
    update_user     TEXT        DEFAULT 'system'
);

CREATE INDEX IF NOT EXISTS idx_asq_owner
    ON audit_saved_query(owner_id);

CREATE INDEX IF NOT EXISTS idx_asq_public
    ON audit_saved_query(is_public);

-- ============================================================
-- audit_search_history: 用户搜索历史
-- ============================================================
CREATE TABLE IF NOT EXISTS audit_search_history (
    id              TEXT        PRIMARY KEY NOT NULL,
    user_id         TEXT        NOT NULL,                           -- 用户 ID
    query_json      TEXT        NOT NULL,                           -- AuditQuery DSL 序列化为 JSON
    searched_at     TEXT        NOT NULL,                           -- 搜索时间（ISO-8601）
    create_time     TEXT        NOT NULL DEFAULT (datetime('now','localtime')),
    update_time     TEXT        NOT NULL DEFAULT (datetime('now','localtime')),
    create_user     TEXT        DEFAULT 'system',
    update_user     TEXT        DEFAULT 'system'
);

CREATE INDEX IF NOT EXISTS idx_ash_user_time
    ON audit_search_history(user_id, searched_at DESC);

-- ============================================================
-- 性能索引：P4 常用复合查询
-- ============================================================

-- 复合索引：模块+操作+时间（最常见组合查询）
CREATE INDEX IF NOT EXISTS idx_ae_module_action_time
    ON audit_event(module, action, created_at DESC);

-- 复合索引：操作人+时间（人员行为调查）
CREATE INDEX IF NOT EXISTS idx_ae_operator_time
    ON audit_event(operator_name, created_at DESC);