package com.github.houbb.core.audit.application.domain.enterprise;

/**
 * P9 Webhook/Streaming 订阅领域对象
 * <p>管理事件订阅和 Webhook 通知配置。</p>
 */
public class AuditSubscription {

    private String id;
    private String subscriber;
    private String eventType;
    private String module;
    private String target;           // WEBHOOK / QUEUE / LOCAL / STREAM
    private String targetUrl;
    private String secret;
    private Integer retryCount;
    private Integer timeoutMs;
    private Integer enabled;         // 0/1
    private String lastSentAt;
    private String lastStatus;
    private String errorMessage;

    private AuditSubscription() {}

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private final AuditSubscription sub = new AuditSubscription();
        public Builder id(String v) { sub.id = v; return this; }
        public Builder subscriber(String v) { sub.subscriber = v; return this; }
        public Builder eventType(String v) { sub.eventType = v; return this; }
        public Builder module(String v) { sub.module = v; return this; }
        public Builder target(String v) { sub.target = v; return this; }
        public Builder targetUrl(String v) { sub.targetUrl = v; return this; }
        public Builder secret(String v) { sub.secret = v; return this; }
        public Builder retryCount(Integer v) { sub.retryCount = v; return this; }
        public Builder timeoutMs(Integer v) { sub.timeoutMs = v; return this; }
        public Builder enabled(Integer v) { sub.enabled = v; return this; }
        public Builder lastSentAt(String v) { sub.lastSentAt = v; return this; }
        public Builder lastStatus(String v) { sub.lastStatus = v; return this; }
        public Builder errorMessage(String v) { sub.errorMessage = v; return this; }
        public AuditSubscription build() { return sub; }
    }

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
}
