package com.github.houbb.core.audit.application.domain.enterprise;

/**
 * P9 Webhook 投递日志领域对象
 * <p>记录每次 Webhook POST 的详细投递日志。</p>
 */
public class WebhookDelivery {

    private String id;
    private String subscriptionId;
    private String auditId;
    private String requestUrl;
    private String requestBody;
    private Integer responseCode;
    private String responseBody;
    private Integer durationMs;
    private String status;      // PENDING / SUCCESS / FAIL / RETRYING
    private Integer attempt;
    private String errorMessage;
    private String sentAt;

    private WebhookDelivery() {}

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private final WebhookDelivery d = new WebhookDelivery();
        public Builder id(String v) { d.id = v; return this; }
        public Builder subscriptionId(String v) { d.subscriptionId = v; return this; }
        public Builder auditId(String v) { d.auditId = v; return this; }
        public Builder requestUrl(String v) { d.requestUrl = v; return this; }
        public Builder requestBody(String v) { d.requestBody = v; return this; }
        public Builder responseCode(Integer v) { d.responseCode = v; return this; }
        public Builder responseBody(String v) { d.responseBody = v; return this; }
        public Builder durationMs(Integer v) { d.durationMs = v; return this; }
        public Builder status(String v) { d.status = v; return this; }
        public Builder attempt(Integer v) { d.attempt = v; return this; }
        public Builder errorMessage(String v) { d.errorMessage = v; return this; }
        public Builder sentAt(String v) { d.sentAt = v; return this; }
        public WebhookDelivery build() { return d; }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(String subscriptionId) { this.subscriptionId = subscriptionId; }
    public String getAuditId() { return auditId; }
    public void setAuditId(String auditId) { this.auditId = auditId; }
    public String getRequestUrl() { return requestUrl; }
    public void setRequestUrl(String requestUrl) { this.requestUrl = requestUrl; }
    public String getRequestBody() { return requestBody; }
    public void setRequestBody(String requestBody) { this.requestBody = requestBody; }
    public Integer getResponseCode() { return responseCode; }
    public void setResponseCode(Integer responseCode) { this.responseCode = responseCode; }
    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }
    public Integer getDurationMs() { return durationMs; }
    public void setDurationMs(Integer durationMs) { this.durationMs = durationMs; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getAttempt() { return attempt; }
    public void setAttempt(Integer attempt) { this.attempt = attempt; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public String getSentAt() { return sentAt; }
    public void setSentAt(String sentAt) { this.sentAt = sentAt; }
}
