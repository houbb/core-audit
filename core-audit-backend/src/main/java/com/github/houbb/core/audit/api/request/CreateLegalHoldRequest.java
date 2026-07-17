package com.github.houbb.core.audit.api.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 创建法律保留请求
 */
public class CreateLegalHoldRequest {

    @NotBlank
    private String auditId;

    @NotBlank
    private String reason;

    private String owner;

    /** 保留结束时间（ISO-8601），null = 永久 */
    private String expiredAt;

    public String getAuditId() { return auditId; }
    public void setAuditId(String auditId) { this.auditId = auditId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
    public String getExpiredAt() { return expiredAt; }
    public void setExpiredAt(String expiredAt) { this.expiredAt = expiredAt; }
}