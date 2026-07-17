package com.github.houbb.core.audit.api.response;

import com.github.houbb.core.audit.application.domain.compliance.LegalHold;

import java.time.LocalDateTime;

/**
 * 法律保留响应
 */
public class LegalHoldResponse {

    private String id;
    private String auditId;
    private String reason;
    private String owner;
    private LocalDateTime expiredAt;

    public static LegalHoldResponse from(LegalHold hold) {
        LegalHoldResponse resp = new LegalHoldResponse();
        resp.id = hold.getId();
        resp.auditId = hold.getAuditId();
        resp.reason = hold.getReason();
        resp.owner = hold.getOwner();
        resp.expiredAt = hold.getExpiredAt();
        return resp;
    }

    public String getId() { return id; }
    public String getAuditId() { return auditId; }
    public String getReason() { return reason; }
    public String getOwner() { return owner; }
    public LocalDateTime getExpiredAt() { return expiredAt; }
}