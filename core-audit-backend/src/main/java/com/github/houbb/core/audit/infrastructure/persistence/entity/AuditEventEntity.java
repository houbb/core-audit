package com.github.houbb.core.audit.infrastructure.persistence.entity;

/**
 * 审计事件持久化实体
 * <p>直接映射 audit_event 表字段。</p>
 */
public class AuditEventEntity {

    private String id;
    private String module;
    private String action;
    private String targetType;
    private String targetId;
    private String operatorId;
    private String operatorName;
    private String result;
    private String description;
    private String clientIp;
    private String requestUri;
    private String requestMethod;
    private String traceId;
    private String metadata;        // JSON

    // ======== P1 event fields ========
    private String eventId;         // 事件 ID（区别于记录 ID）
    private String eventType;       // 事件类型枚举名
    private String source;          // 来源服务名
    private String version;         // 事件版本
    private String occurredAt;      // 业务发生时间
    private Integer published;      // 是否已发布（0/1）
    private String publishTime;     // 发布时间
    private String publishResult;   // 发布结果

    private String createdAt;       // ISO-8601
    private String createTime;
    private String updateTime;
    private String createUser;
    private String updateUser;

    // ======== P2 context field ========
    private String contextJson;         // JSON representation of AuditContext

    // ======== Getters & Setters ========

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public String getOperatorId() { return operatorId; }
    public void setOperatorId(String operatorId) { this.operatorId = operatorId; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getClientIp() { return clientIp; }
    public void setClientIp(String clientIp) { this.clientIp = clientIp; }
    public String getRequestUri() { return requestUri; }
    public void setRequestUri(String requestUri) { this.requestUri = requestUri; }
    public String getRequestMethod() { return requestMethod; }
    public void setRequestMethod(String requestMethod) { this.requestMethod = requestMethod; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    // ======== P1 fields ========
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getOccurredAt() { return occurredAt; }
    public void setOccurredAt(String occurredAt) { this.occurredAt = occurredAt; }
    public Integer getPublished() { return published; }
    public void setPublished(Integer published) { this.published = published; }
    public String getPublishTime() { return publishTime; }
    public void setPublishTime(String publishTime) { this.publishTime = publishTime; }
    public String getPublishResult() { return publishResult; }
    public void setPublishResult(String publishResult) { this.publishResult = publishResult; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
    public String getCreateUser() { return createUser; }
    public void setCreateUser(String createUser) { this.createUser = createUser; }
    public String getUpdateUser() { return updateUser; }
    public void setUpdateUser(String updateUser) { this.updateUser = updateUser; }

    // ======== P2 getters & setters ========
    public String getContextJson() { return contextJson; }
    public void setContextJson(String contextJson) { this.contextJson = contextJson; }
}