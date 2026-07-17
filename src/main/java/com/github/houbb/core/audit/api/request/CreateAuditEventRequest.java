package com.github.houbb.core.audit.api.request;

import com.github.houbb.core.audit.application.domain.enums.AuditAction;
import com.github.houbb.core.audit.application.domain.enums.AuditEventType;
import com.github.houbb.core.audit.application.domain.enums.AuditModule;
import com.github.houbb.core.audit.application.domain.enums.AuditResult;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 创建审计事件请求
 */
public class CreateAuditEventRequest {

    /** 事件 ID（可选，不传则服务端生成） */
    private String id;

    /** 所属模块（必填） */
    @NotNull(message = "module 不能为空")
    private AuditModule module;

    /** 操作类型（必填） */
    @NotNull(message = "action 不能为空")
    private AuditAction action;

    /** 目标对象类型 */
    private String targetType;

    /** 目标对象 ID */
    private String targetId;

    /** 操作人 ID */
    private String operatorId;

    /** 操作人名称 */
    private String operatorName;

    /** 执行结果（必填） */
    @NotNull(message = "result 不能为空")
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

    /** 审计事件发生时间（可选，不传则当前时间） */
    private LocalDateTime createdAt;

    /** JSON 扩展信息 */
    private Map<String, Object> metadata;

    // ======== P1 fields ========

    /** 事件类型（P1 新增） */
    private AuditEventType eventType;

    /** 来源服务名（P1 新增 — 如 "core-user"） */
    private String source;

    /** 事件版本（P1 新增 — 默认 "1.0"） */
    private String version;

    /** 是否发布事件（P1 新增 — 默认 true） */
    private Boolean publish = true;

    // ======== Getters & Setters ========

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public AuditModule getModule() { return module; }
    public void setModule(AuditModule module) { this.module = module; }
    public AuditAction getAction() { return action; }
    public void setAction(AuditAction action) { this.action = action; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public String getOperatorId() { return operatorId; }
    public void setOperatorId(String operatorId) { this.operatorId = operatorId; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public AuditResult getResult() { return result; }
    public void setResult(AuditResult result) { this.result = result; }
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
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    // ======== P1 getters & setters ========

    public AuditEventType getEventType() { return eventType; }
    public void setEventType(AuditEventType eventType) { this.eventType = eventType; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public Boolean getPublish() { return publish; }
    public void setPublish(Boolean publish) { this.publish = publish; }
}