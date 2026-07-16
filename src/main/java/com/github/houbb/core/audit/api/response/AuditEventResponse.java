package com.github.houbb.core.audit.api.response;

import com.github.houbb.core.audit.application.domain.AuditEvent;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 审计事件响应
 * <p>与领域对象字段一致，但只暴露 API 需要的字段。</p>
 */
public class AuditEventResponse {

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
    private LocalDateTime createdAt;
    private Map<String, Object> metadata;

    /**
     * 从领域对象构造
     */
    public static AuditEventResponse from(AuditEvent event) {
        if (event == null) return null;
        AuditEventResponse resp = new AuditEventResponse();
        resp.id = event.getId();
        resp.module = event.getModule() != null ? event.getModule().name() : null;
        resp.action = event.getAction() != null ? event.getAction().name() : null;
        resp.targetType = event.getTargetType();
        resp.targetId = event.getTargetId();
        resp.operatorId = event.getOperatorId();
        resp.operatorName = event.getOperatorName();
        resp.result = event.getResult() != null ? event.getResult().name() : null;
        resp.description = event.getDescription();
        resp.clientIp = event.getClientIp();
        resp.requestUri = event.getRequestUri();
        resp.requestMethod = event.getRequestMethod();
        resp.traceId = event.getTraceId();
        resp.createdAt = event.getCreatedAt();
        resp.metadata = event.getMetadata();
        return resp;
    }

    // ======== Getters ========

    public String getId() { return id; }
    public String getModule() { return module; }
    public String getAction() { return action; }
    public String getTargetType() { return targetType; }
    public String getTargetId() { return targetId; }
    public String getOperatorId() { return operatorId; }
    public String getOperatorName() { return operatorName; }
    public String getResult() { return result; }
    public String getDescription() { return description; }
    public String getClientIp() { return clientIp; }
    public String getRequestUri() { return requestUri; }
    public String getRequestMethod() { return requestMethod; }
    public String getTraceId() { return traceId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Map<String, Object> getMetadata() { return metadata; }
}