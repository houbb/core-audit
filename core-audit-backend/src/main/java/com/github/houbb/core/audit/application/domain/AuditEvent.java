package com.github.houbb.core.audit.application.domain;

import com.github.houbb.core.audit.application.domain.context.AuditContext;
import com.github.houbb.core.audit.application.domain.enums.AuditAction;
import com.github.houbb.core.audit.application.domain.enums.AuditEventType;
import com.github.houbb.core.audit.application.domain.enums.AuditModule;
import com.github.houbb.core.audit.application.domain.enums.AuditResult;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 审计事件领域对象
 * <p>整个 Audit Runtime 最核心的领域模型。</p>
 */
public class AuditEvent {

    /** 事件 ID（UUID） */
    private String id;

    /** 所属模块 */
    private AuditModule module;

    /** 操作类型 */
    private AuditAction action;

    /** 事件类型（P1 新增 — 业务语义级别的事件类型） */
    private AuditEventType eventType;

    /** 事件 ID（P1 新增 — 区别于记录 ID，用于事件路由和去重） */
    private String eventId;

    /** 来源服务名（P1 新增 — 如 "core-user"、"core-file"） */
    private String source;

    /** 事件版本（P1 新增 — 默认 "1.0"） */
    private String version;

    /** 业务发生时间（P1 新增） */
    private LocalDateTime occurredAt;

    /** 是否已发布（P1 新增） */
    private Boolean publish = true;

    /** 发布时间（P1 新增） */
    private LocalDateTime publishTime;

    /** 发布结果（P1 新增 — SUCCESS/FAIL） */
    private String publishResult;

    /** 目标对象类型 */
    private String targetType;

    /** 目标对象 ID */
    private String targetId;

    /** 操作人 ID */
    private String operatorId;

    /** 操作人名称 */
    private String operatorName;

    /** 执行结果 */
    private AuditResult result;

    /** 操作描述 */
    private String description;

    /** 客户端 IP */
    private String clientIp;

    /** 请求 URI */
    private String requestUri;

    /** 请求方法 */
    private String requestMethod;

    /** 分布式 Trace ID */
    private String traceId;

    /** 审计事件发生时间 */
    private LocalDateTime createdAt;

    /** JSON 扩展信息 */
    private Map<String, Object> metadata;

    /** P2 统一上下文（Operator + Request + Client + Business + System） */
    private AuditContext context;

    /** P9 租户标识（多租户隔离，默认 "default"） */
    private String tenant;

    // ======== Constructors ========

    public AuditEvent() {
    }

    private AuditEvent(Builder builder) {
        this.id = builder.id;
        this.module = builder.module;
        this.action = builder.action;
        this.eventType = builder.eventType;
        this.eventId = builder.eventId;
        this.source = builder.source;
        this.version = builder.version;
        this.occurredAt = builder.occurredAt;
        this.publish = builder.publish;
        this.publishTime = builder.publishTime;
        this.publishResult = builder.publishResult;
        this.targetType = builder.targetType;
        this.targetId = builder.targetId;
        this.operatorId = builder.operatorId;
        this.operatorName = builder.operatorName;
        this.result = builder.result;
        this.description = builder.description;
        this.clientIp = builder.clientIp;
        this.requestUri = builder.requestUri;
        this.requestMethod = builder.requestMethod;
        this.traceId = builder.traceId;
        this.createdAt = builder.createdAt;
        this.metadata = builder.metadata;
        this.context = builder.context;
        this.tenant = builder.tenant;
    }

    // ======== Builder ========

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private AuditModule module;
        private AuditAction action;
        private AuditEventType eventType;
        private String eventId;
        private String source;
        private String version;
        private LocalDateTime occurredAt;
        private Boolean publish = true;
        private LocalDateTime publishTime;
        private String publishResult;
        private String targetType;
        private String targetId;
        private String operatorId;
        private String operatorName;
        private AuditResult result;
        private String description;
        private String clientIp;
        private String requestUri;
        private String requestMethod;
        private String traceId;
        private LocalDateTime createdAt;
        private Map<String, Object> metadata;
        private AuditContext context;
        private String tenant;

        public Builder id(String id) { this.id = id; return this; }
        public Builder module(AuditModule module) { this.module = module; return this; }
        public Builder action(AuditAction action) { this.action = action; return this; }
        public Builder eventType(AuditEventType eventType) { this.eventType = eventType; return this; }
        public Builder eventId(String eventId) { this.eventId = eventId; return this; }
        public Builder source(String source) { this.source = source; return this; }
        public Builder version(String version) { this.version = version; return this; }
        public Builder occurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; return this; }
        public Builder publish(Boolean publish) { this.publish = publish; return this; }
        public Builder publishTime(LocalDateTime publishTime) { this.publishTime = publishTime; return this; }
        public Builder publishResult(String publishResult) { this.publishResult = publishResult; return this; }
        public Builder targetType(String targetType) { this.targetType = targetType; return this; }
        public Builder targetId(String targetId) { this.targetId = targetId; return this; }
        public Builder operatorId(String operatorId) { this.operatorId = operatorId; return this; }
        public Builder operatorName(String operatorName) { this.operatorName = operatorName; return this; }
        public Builder result(AuditResult result) { this.result = result; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder clientIp(String clientIp) { this.clientIp = clientIp; return this; }
        public Builder requestUri(String requestUri) { this.requestUri = requestUri; return this; }
        public Builder requestMethod(String requestMethod) { this.requestMethod = requestMethod; return this; }
        public Builder traceId(String traceId) { this.traceId = traceId; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder metadata(Map<String, Object> metadata) { this.metadata = metadata; return this; }
        public Builder context(AuditContext context) { this.context = context; return this; }
        public Builder tenant(String tenant) { this.tenant = tenant; return this; }

        public AuditEvent build() {
            return new AuditEvent(this);
        }
    }

    // ======== Getters ========

    public String getId() { return id; }
    public AuditModule getModule() { return module; }
    public AuditAction getAction() { return action; }
    public AuditEventType getEventType() { return eventType; }
    public String getEventId() { return eventId; }
    public String getSource() { return source; }
    public String getVersion() { return version; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public Boolean isPublish() { return publish; }
    public LocalDateTime getPublishTime() { return publishTime; }
    public String getPublishResult() { return publishResult; }
    public String getTargetType() { return targetType; }
    public String getTargetId() { return targetId; }
    public String getOperatorId() { return operatorId; }
    public String getOperatorName() { return operatorName; }
    public AuditResult getResult() { return result; }
    public String getDescription() { return description; }
    public String getClientIp() { return clientIp; }
    public String getRequestUri() { return requestUri; }
    public String getRequestMethod() { return requestMethod; }
    public String getTraceId() { return traceId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Map<String, Object> getMetadata() { return metadata; }
    public AuditContext getContext() { return context; }
    public String getTenant() { return tenant; }

    // ======== Setters ========

    public void setId(String id) { this.id = id; }
    public void setModule(AuditModule module) { this.module = module; }
    public void setAction(AuditAction action) { this.action = action; }
    public void setEventType(AuditEventType eventType) { this.eventType = eventType; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public void setSource(String source) { this.source = source; }
    public void setVersion(String version) { this.version = version; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }
    public void setPublish(Boolean publish) { this.publish = publish; }
    public void setPublishTime(LocalDateTime publishTime) { this.publishTime = publishTime; }
    public void setPublishResult(String publishResult) { this.publishResult = publishResult; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public void setOperatorId(String operatorId) { this.operatorId = operatorId; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public void setResult(AuditResult result) { this.result = result; }
    public void setDescription(String description) { this.description = description; }
    public void setClientIp(String clientIp) { this.clientIp = clientIp; }
    public void setRequestUri(String requestUri) { this.requestUri = requestUri; }
    public void setRequestMethod(String requestMethod) { this.requestMethod = requestMethod; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    public void setContext(AuditContext context) { this.context = context; }
    public void setTenant(String tenant) { this.tenant = tenant; }
}