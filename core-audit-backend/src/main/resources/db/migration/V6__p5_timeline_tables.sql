-- ============================================================
-- V6: P5 Timeline Runtime — 时间线聚合表
-- Description: Create audit_timeline, audit_timeline_event tables
--              for behavior timeline reconstruction
-- ============================================================

-- ============================================================
-- audit_timeline: 时间线主表
-- 一条 Timeline 聚合多个 Audit Event，表达一次完整业务过程
-- ============================================================
CREATE TABLE IF NOT EXISTS audit_timeline (
    id              TEXT        PRIMARY KEY NOT NULL,
    type            TEXT        NOT NULL,                           -- TimelineType 枚举名称
    title           TEXT,                                           -- 自动生成标题
    start_time      TEXT,                                           -- 最早事件时间（ISO-8601）
    end_time        TEXT,                                           -- 最晚事件时间（ISO-8601）
    duration        INTEGER,                                        -- 总耗时（毫秒）
    summary         TEXT,                                           -- 摘要（P8 由 AI 生成）
    create_time     TEXT        NOT NULL DEFAULT (datetime('now','localtime')),
    update_time     TEXT        NOT NULL DEFAULT (datetime('now','localtime')),
    create_user     TEXT        DEFAULT 'system',
    update_user     TEXT        DEFAULT 'system'
);

CREATE INDEX IF NOT EXISTS idx_timeline_type
    ON audit_timeline(type);

-- ============================================================
-- audit_timeline_event: Timeline 与 Audit Event 多对多关联
-- 一个 Event 可属于多条 Timeline（多归属）
-- ============================================================
CREATE TABLE IF NOT EXISTS audit_timeline_event (
    id              TEXT        PRIMARY KEY NOT NULL,
    timeline_id     TEXT        NOT NULL,                           -- FK → audit_timeline.id
    audit_id        TEXT        NOT NULL,                           -- FK → audit_event.id
    sequence        INTEGER     NOT NULL,                           -- 在 Timeline 中的序号
    create_time     TEXT        NOT NULL DEFAULT (datetime('now','localtime')),
    update_time     TEXT        NOT NULL DEFAULT (datetime('now','localtime')),
    create_user     TEXT        DEFAULT 'system',
    update_user     TEXT        DEFAULT 'system',
    FOREIGN KEY (timeline_id) REFERENCES audit_timeline(id),
    FOREIGN KEY (audit_id) REFERENCES audit_event(id)
);

CREATE INDEX IF NOT EXISTS idx_tle_timeline
    ON audit_timeline_event(timeline_id, sequence);

CREATE INDEX IF NOT EXISTS idx_tle_audit
    ON audit_timeline_event(audit_id);
