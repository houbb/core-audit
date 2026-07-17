package com.github.houbb.core.audit.application.domain.compliance;

import java.time.LocalDateTime;

/**
 * 法律保留
 * <p>法律保留期间，即使数据保留策略到期，被持有的审计记录也不能删除。</p>
 */
public class LegalHold {

    private String id;
    private String auditId;
    private String reason;
    private String owner;
    private LocalDateTime expiredAt;   // null = 永久保留
    private LocalDateTime createdAt;

    public LegalHold() {
    }

    private LegalHold(Builder builder) {
        this.id = builder.id;
        this.auditId = builder.auditId;
        this.reason = builder.reason;
        this.owner = builder.owner;
        this.expiredAt = builder.expiredAt;
        this.createdAt = builder.createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private String auditId;
        private String reason;
        private String owner;
        private LocalDateTime expiredAt;
        private LocalDateTime createdAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder auditId(String auditId) { this.auditId = auditId; return this; }
        public Builder reason(String reason) { this.reason = reason; return this; }
        public Builder owner(String owner) { this.owner = owner; return this; }
        public Builder expiredAt(LocalDateTime expiredAt) { this.expiredAt = expiredAt; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public LegalHold build() {
            return new LegalHold(this);
        }
    }

    // ======== Getters & Setters ========

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAuditId() { return auditId; }
    public void setAuditId(String auditId) { this.auditId = auditId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
    public LocalDateTime getExpiredAt() { return expiredAt; }
    public void setExpiredAt(LocalDateTime expiredAt) { this.expiredAt = expiredAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
