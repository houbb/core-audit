-- ============================================================
-- V10: P9 Enterprise Audit Platform — 企业统一审计平台
-- Description: 平台化升级
--   1. audit_event 增加 tenant 字段（多租户隔离）
--   2. audit_source — 审计来源系统注册表
--   3. audit_provider — 插件/Marketplace 注册表
--   4. audit_subscription — Webhook/Streaming 订阅表
--   5. audit_webhook_config — Webhook 配置表
-- ============================================================

-- ============================================================
-- 1. audit_event 增加 tenant 列（多租户隔离）
-- ============================================================
ALTER TABLE audit_event ADD COLUMN tenant TEXT DEFAULT 'default';

CREATE INDEX IF NOT EXISTS idx_ae_tenant
    ON audit_event(tenant);

CREATE INDEX IF NOT EXISTS idx_ae_tenant_time
    ON audit_event(tenant, created_at DESC);

-- ============================================================
-- 2. audit_source: 审计来源系统注册表
-- 记录每个接入 core-audit 的系统身份信息
-- ============================================================
CREATE TABLE IF NOT EXISTS audit_source (
    id              TEXT        PRIMARY KEY NOT NULL,
    name            TEXT        NOT NULL,                           -- 来源系统名称（如 "core-user"）
    type            TEXT        NOT NULL DEFAULT 'INTERNAL',        -- INTERNAL / EXTERNAL / PLUGIN / AGENT / THIRD_PARTY
    version         TEXT        NOT NULL DEFAULT '1.0',             -- SDK 版本
    tenant          TEXT        NOT NULL DEFAULT 'default',         -- 所属租户
    description     TEXT,                                           -- 系统描述
    endpoint        TEXT,                                           -- 系统回调地址（用于 Global Search 等）
    auth_token      TEXT,                                           -- 认证 Token（用于回调认证）
    status          TEXT        NOT NULL DEFAULT 'ACTIVE',          -- ACTIVE / INACTIVE / SUSPENDED
    registered_at   TEXT        NOT NULL,                           -- 注册时间（ISO-8601）
    last_seen_at    TEXT,                                           -- 最后活跃时间
    metadata_json   TEXT,                                           -- 扩展元数据 JSON
    create_time     TEXT        NOT NULL DEFAULT (datetime('now','localtime')),
    update_time     TEXT        NOT NULL DEFAULT (datetime('now','localtime')),
    create_user     TEXT        DEFAULT 'system',
    update_user     TEXT        DEFAULT 'system'
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_source_name_tenant
    ON audit_source(name, tenant);

CREATE INDEX IF NOT EXISTS idx_source_type
    ON audit_source(type);

CREATE INDEX IF NOT EXISTS idx_source_status
    ON audit_source(status);

-- ============================================================
-- 3. audit_provider: 插件/Marketplace 注册表
-- 管理已安装的 AuditProvider / SPI 插件
-- ============================================================
CREATE TABLE IF NOT EXISTS audit_provider (
    id              TEXT        PRIMARY KEY NOT NULL,
    plugin          TEXT        NOT NULL,                           -- 插件名称（如 "sap-audit"）
    provider_class  TEXT        NOT NULL,                           -- Provider SPI 实现类全限定名
    provider_type   TEXT        NOT NULL DEFAULT 'PROVIDER',        -- PROVIDER / TIMELINE_STRATEGY / REPLAY_STRATEGY / COMPLIANCE_PROVIDER / AUDIT_AGENT
    version         TEXT        NOT NULL DEFAULT '1.0',             -- 插件版本
    tenant          TEXT        NOT NULL DEFAULT 'default',         -- 所属租户
    description     TEXT,                                           -- 插件描述
    author          TEXT,                                           -- 插件作者
    config_json     TEXT,                                           -- 插件配置 JSON
    status          TEXT        NOT NULL DEFAULT 'ACTIVE',          -- ACTIVE / INACTIVE / ERROR
    installed_at    TEXT        NOT NULL,                           -- 安装时间
    create_time     TEXT        NOT NULL DEFAULT (datetime('now','localtime')),
    update_time     TEXT        NOT NULL DEFAULT (datetime('now','localtime')),
    create_user     TEXT        DEFAULT 'system',
    update_user     TEXT        DEFAULT 'system'
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_provider_plugin_tenant
    ON audit_provider(plugin, tenant);

CREATE INDEX IF NOT EXISTS idx_provider_type
    ON audit_provider(provider_type);

CREATE INDEX IF NOT EXISTS idx_provider_status
    ON audit_provider(status);

-- ============================================================
-- 4. audit_subscription: Webhook/Streaming 订阅表
-- 管理 Webhook 和 Streaming 的事件订阅
-- ============================================================
CREATE TABLE IF NOT EXISTS audit_subscription (
    id              TEXT        PRIMARY KEY NOT NULL,
    subscriber      TEXT        NOT NULL,                           -- 订阅方名称（如 "Slack", "Teams", "RCA"）
    event_type      TEXT,                                           -- 订阅的事件类型（NULL = 所有事件）
    module          TEXT,                                           -- 订阅的模块（NULL = 所有模块）
    target          TEXT        NOT NULL DEFAULT 'WEBHOOK',         -- WEBHOOK / QUEUE / LOCAL / STREAM
    target_url      TEXT,                                           -- Webhook URL 或 Queue Topic
    secret          TEXT,                                           -- Webhook Secret（HMAC 签名）
    retry_count     INTEGER     DEFAULT 3,                          -- 最大重试次数
    timeout_ms      INTEGER     DEFAULT 5000,                       -- 超时时间（毫秒）
    enabled         INTEGER     NOT NULL DEFAULT 1,                 -- 是否启用（0/1）
    last_sent_at    TEXT,                                           -- 最后发送时间
    last_status     TEXT,                                           -- 最后发送状态
    error_message   TEXT,                                           -- 最近错误信息
    create_time     TEXT        NOT NULL DEFAULT (datetime('now','localtime')),
    update_time     TEXT        NOT NULL DEFAULT (datetime('now','localtime')),
    create_user     TEXT        DEFAULT 'system',
    update_user     TEXT        DEFAULT 'system'
);

CREATE INDEX IF NOT EXISTS idx_sub_enabled
    ON audit_subscription(enabled);

CREATE INDEX IF NOT EXISTS idx_sub_event_type
    ON audit_subscription(event_type);

CREATE INDEX IF NOT EXISTS idx_sub_module
    ON audit_subscription(module);

-- ============================================================
-- 5. audit_webhook_delivery: Webhook 投递日志表
-- 每次 Webhook POST 的详细日志
-- ============================================================
CREATE TABLE IF NOT EXISTS audit_webhook_delivery (
    id              TEXT        PRIMARY KEY NOT NULL,
    subscription_id TEXT        NOT NULL,                           -- FK → audit_subscription.id
    audit_id        TEXT        NOT NULL,                           -- FK → audit_event.id
    request_url     TEXT        NOT NULL,                           -- 目标 URL
    request_body    TEXT,                                           -- 请求体
    response_code   INTEGER,                                        -- HTTP 响应码
    response_body   TEXT,                                           -- 响应体
    duration_ms     INTEGER,                                        -- 耗时（毫秒）
    status          TEXT        NOT NULL DEFAULT 'PENDING',         -- PENDING / SUCCESS / FAIL / RETRYING
    attempt         INTEGER     DEFAULT 1,                          -- 第几次尝试
    error_message   TEXT,                                           -- 错误信息
    sent_at         TEXT        NOT NULL,                           -- 发送时间
    create_time     TEXT        NOT NULL DEFAULT (datetime('now','localtime')),
    update_time     TEXT        NOT NULL DEFAULT (datetime('now','localtime')),
    create_user     TEXT        DEFAULT 'system',
    update_user     TEXT        DEFAULT 'system'
);

CREATE INDEX IF NOT EXISTS idx_webhook_delivery_sub
    ON audit_webhook_delivery(subscription_id);

CREATE INDEX IF NOT EXISTS idx_webhook_delivery_audit
    ON audit_webhook_delivery(audit_id);

CREATE INDEX IF NOT EXISTS idx_webhook_delivery_status
    ON audit_webhook_delivery(status);
