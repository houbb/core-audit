-- ============================================================
-- V9: P8 Intelligence Runtime — 智能分析表
-- Description: Create audit_risk, audit_insight, audit_pattern tables
--              for AI-powered risk analysis, anomaly detection, and
--              behavioral pattern recognition
-- ============================================================

-- ============================================================
-- audit_risk: 风险评分表
-- 每条 Audit Event 的风险评分 (0-100) 及风险等级
-- ============================================================
CREATE TABLE IF NOT EXISTS audit_risk (
    id              TEXT        PRIMARY KEY NOT NULL,
    audit_id        TEXT        NOT NULL,                           -- FK → audit_event.id
    risk_score      INTEGER     NOT NULL DEFAULT 0,                 -- 风险评分 0-100
    risk_level      TEXT        NOT NULL DEFAULT 'LOW',             -- LOW / MEDIUM / HIGH / CRITICAL
    reason          TEXT,                                           -- 风险原因（可解释的）
    rule_name       TEXT,                                           -- 触发的规则名称（规则命中时）
    ai_analysis     TEXT,                                           -- AI 分析文本（AI 增强时）
    analyzed_at     TEXT        NOT NULL,                           -- 分析时间（ISO-8601）
    create_time     TEXT        NOT NULL DEFAULT (datetime('now','localtime')),
    update_time     TEXT        NOT NULL DEFAULT (datetime('now','localtime')),
    create_user     TEXT        DEFAULT 'system',
    update_user     TEXT        DEFAULT 'system'
);

CREATE INDEX IF NOT EXISTS idx_risk_audit
    ON audit_risk(audit_id);

CREATE INDEX IF NOT EXISTS idx_risk_level
    ON audit_risk(risk_level);

CREATE INDEX IF NOT EXISTS idx_risk_score
    ON audit_risk(risk_score);

-- ============================================================
-- audit_insight: 智能洞察表
-- AI 生成的洞察（异常发现、安全分析、行为建议）
-- ============================================================
CREATE TABLE IF NOT EXISTS audit_insight (
    id              TEXT        PRIMARY KEY NOT NULL,
    audit_id        TEXT,                                           -- FK → audit_event.id（NULL = 全局 Insight）
    title           TEXT        NOT NULL,                           -- 洞察标题
    severity        TEXT        NOT NULL DEFAULT 'LOW',             -- LOW / MEDIUM / HIGH / CRITICAL
    summary         TEXT,                                           -- 摘要描述
    suggestion      TEXT,                                           -- 改进建议
    evidence_json   TEXT,                                           -- Evidence JSON（引用 Timeline/Replay/Diff/Audit ID）
    agent_name      TEXT,                                           -- 生成此 Insight 的 Agent 名称
    create_time     TEXT        NOT NULL DEFAULT (datetime('now','localtime')),
    update_time     TEXT        NOT NULL DEFAULT (datetime('now','localtime')),
    create_user     TEXT        DEFAULT 'system',
    update_user     TEXT        DEFAULT 'system'
);

CREATE INDEX IF NOT EXISTS idx_insight_audit
    ON audit_insight(audit_id);

CREATE INDEX IF NOT EXISTS idx_insight_severity
    ON audit_insight(severity);

-- ============================================================
-- audit_pattern: 行为模式表
-- 记录操作人/资源/工作流的行为模式，用于异常检测
-- ============================================================
CREATE TABLE IF NOT EXISTS audit_pattern (
    id              TEXT        PRIMARY KEY NOT NULL,
    type            TEXT        NOT NULL,                           -- OPERATOR / RESOURCE / WORKFLOW / API / TENANT
    owner           TEXT,                                           -- 模式归属者（如操作人 ID）
    content_json    TEXT        NOT NULL,                           -- Pattern JSON（行为模式数据）
    confidence      REAL        DEFAULT 0.0,                        -- 置信度 0.0-1.0
    sample_count    INTEGER     DEFAULT 0,                          -- 样本数量
    create_time     TEXT        NOT NULL DEFAULT (datetime('now','localtime')),
    update_time     TEXT        NOT NULL DEFAULT (datetime('now','localtime')),
    create_user     TEXT        DEFAULT 'system',
    update_user     TEXT        DEFAULT 'system'
);

CREATE INDEX IF NOT EXISTS idx_pattern_type_owner
    ON audit_pattern(type, owner);