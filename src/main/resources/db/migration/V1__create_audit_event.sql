-- ============================================================
-- V1: 创建 audit_event 核心表
-- 描述：P0 MVP 唯一核心表，存储所有审计事件
-- ============================================================

CREATE TABLE IF NOT EXISTS audit_event (
    -- 主键
    id              TEXT        PRIMARY KEY NOT NULL,

    -- 审计核心字段
    module          TEXT        NOT NULL,                          -- 所属模块：USER/CONFIG/AI/FILE/STORAGE/WORKFLOW/OPENAPI/NOTIFICATION/BILLING
    action          TEXT        NOT NULL,                          -- 操作类型：CREATE/UPDATE/DELETE/LOGIN/LOGOUT/...
    target_type     TEXT,                                          -- 操作对象类型
    target_id       TEXT,                                          -- 操作对象 ID
    operator_id     TEXT,                                          -- 操作人 ID
    operator_name   TEXT,                                          -- 操作人名称
    result          TEXT        NOT NULL,                          -- 执行结果：SUCCESS/FAIL
    description     TEXT,                                          -- 操作描述

    -- 请求上下文
    client_ip       TEXT,                                          -- 客户端 IP
    request_uri     TEXT,                                          -- 请求 URI
    request_method  TEXT,                                          -- 请求方法：GET/POST/PUT/DELETE
    trace_id        TEXT,                                          -- 分布式 Trace ID

    -- 扩展字段
    metadata        TEXT,                                          -- JSON 扩展信息（TEXT 类型兼容 SQLite）

    -- 审计时间（业务时间）
    created_at      TEXT        NOT NULL,                          -- 审计事件发生时间（ISO-8601）

    -- 基础字段（tech 规范要求）
    create_time     TEXT        NOT NULL DEFAULT (datetime('now','localtime')),
    update_time     TEXT        NOT NULL DEFAULT (datetime('now','localtime')),
    create_user     TEXT        DEFAULT 'system',
    update_user     TEXT        DEFAULT 'system'
);

-- ============================================================
-- 索引：按时间查询（最常用）
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_audit_event_created_at
    ON audit_event(created_at DESC);

-- ============================================================
-- 索引：按模块过滤
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_audit_event_module
    ON audit_event(module);

-- ============================================================
-- 索引：按操作过滤
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_audit_event_action
    ON audit_event(action);

-- ============================================================
-- 索引：按操作人过滤
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_audit_event_operator_id
    ON audit_event(operator_id);

-- ============================================================
-- 索引：按结果过滤
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_audit_event_result
    ON audit_event(result);

-- ============================================================
-- 索引：按 trace_id 关联
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_audit_event_trace_id
    ON audit_event(trace_id);

-- ============================================================
-- 复合索引：常见组合查询（模块+操作+时间）
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_audit_event_module_action_time
    ON audit_event(module, action, created_at DESC);

-- ============================================================
-- 索引：按目标对象查询
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_audit_event_target
    ON audit_event(target_type, target_id);