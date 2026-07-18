package com.github.houbb.core.audit.infrastructure.persistence.entity;

/**
 * P9 订阅持久化实体
 * <p>映射 audit_subscription 表。</p>
 */
public class AuditSubscriptionEntity {

    private String id;
    private String subscriber;
    private String eventType;
    private String module;
    private String target;
    private String targetUrl;
    private String secret;
    private Integer retryCount;
    private Integer timeoutMs;
    private Integer enabled;
    private String lastSentAt;
    private String lastStatus;
    private String errorMessage;
    private String createTime;
    private String updateTime;
    private String createUser;
    private String updateUser;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSubscriber() { return subscriber; }
    public void setSubscriber(String subscriber) { this.subscriber = subscriber; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }
    public String getTargetUrl() { return targetUrl; }
    public void setTargetUrl(String targetUrl) { this.targetUrl = targetUrl; }
    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
    public Integer getTimeoutMs() { return timeoutMs; }
    public void setTimeoutMs(Integer timeoutMs) { this.timeoutMs = timeoutMs; }
    public Integer getEnabled() { return enabled; }
    public void setEnabled(Integer enabled) { this.enabled = enabled; }
    public String getLastSentAt() { return lastSentAt; }
    public void setLastSentAt(String lastSentAt) { this.lastSentAt = lastSentAt; }
    public String getLastStatus() { return lastStatus; }
    public void setLastStatus(String lastStatus) { this.lastStatus = lastStatus; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
    public String getCreateUser() { return createUser; }
    public void setCreateUser(String createUser) { this.createUser = createUser; }
    public String getUpdateUser() { return updateUser; }
    public void setUpdateUser(String updateUser) { this.updateUser = updateUser; }
}