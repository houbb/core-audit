-- ============================================================
-- V7: P6 Replay Runtime — 操作重放表
-- Description: Create audit_replay, audit_replay_step tables
--              for operation process reconstruction
-- ============================================================

-- ============================================================
-- audit_replay: Replay 主表
-- 一条 Replay 对应一条 Timeline，记录完整操作过程
-- ============================================================
CREATE TABLE IF NOT EXISTS audit_replay (
    id              TEXT        PRIMARY KEY NOT NULL,
    timeline_id     TEXT        NOT NULL UNIQUE,                   -- FK → audit_timeline.id，一条 Timeline 一条 Replay
    title           TEXT,                                           -- Replay 标题
    duration        INTEGER,                                        -- 总耗时（毫秒）
    created_at      TEXT,                                           -- Replay 生成时间（ISO-8601）
    create_time     TEXT        NOT NULL DEFAULT (datetime('now','localtime')),
    update_time     TEXT        NOT NULL DEFAULT (datetime('now','localtime')),
    create_user     TEXT        DEFAULT 'system',
    update_user     TEXT        DEFAULT 'system',
    FOREIGN KEY (timeline_id) REFERENCES audit_timeline(id)
);

CREATE INDEX IF NOT EXISTS idx_replay_timeline
    ON audit_replay(timeline_id);

-- ============================================================
-- audit_replay_step: Replay 步骤表
-- 按 sequence 排序，构成完整的操作回放序列
-- ============================================================
CREATE TABLE IF NOT EXISTS audit_replay_step (
    id              TEXT        PRIMARY KEY NOT NULL,
    replay_id       TEXT        NOT NULL,                           -- FK → audit_replay.id
    sequence        INTEGER     NOT NULL,                           -- 步骤序号
    step_type       TEXT        NOT NULL,                           -- ReplayStepType 枚举名称
    title           TEXT,                                           -- 步骤标题
    payload_json    TEXT,                                           -- 步骤附加数据（JSON）
    create_time     TEXT        NOT NULL DEFAULT (datetime('now','localtime')),
    update_time     TEXT        NOT NULL DEFAULT (datetime('now','localtime')),
    create_user     TEXT        DEFAULT 'system',
    update_user     TEXT        DEFAULT 'system',
    FOREIGN KEY (replay_id) REFERENCES audit_replay(id)
);

CREATE INDEX IF NOT EXISTS idx_replay_step_replay
    ON audit_replay_step(replay_id, sequence);