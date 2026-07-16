package com.github.houbb.core.audit.application.domain;

import com.github.houbb.core.audit.application.domain.enums.AuditAction;
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

    // ======== Constructors ========

    public AuditEvent() {
    }

    private AuditEvent(Builder builder) {
        this.id = builder.id;
        this.module = builder.module;
        this.action = builder.action;
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
    }

    // ======== Builder ========

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private AuditModule module;
        private AuditAction action;
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

        public Builder id(String id) { this.id = id; return this; }
        public Builder module(AuditModule module) { this.module = module; return this; }
        public Builder action(AuditAction action) { this.action = action; return this; }
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

        public AuditEvent build() {
            return new AuditEvent(this);
        }
    }

    // ======== Getters ========

    public String getId() { return id; }
    public AuditModule getModule() { return module; }
    public AuditAction getAction() { return action; }
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

    // ======== Setters ========

    public void setId(String id) { this.id = id; }
    public void setModule(AuditModule module) { this.module = module; }
    public void setAction(AuditAction action) { this.action = action; }
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
}