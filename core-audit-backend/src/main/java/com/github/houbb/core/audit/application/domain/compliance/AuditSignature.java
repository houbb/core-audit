package com.github.houbb.core.audit.application.domain.compliance;

import java.time.LocalDateTime;

/**
 * 审计事件完整性签名
 * <p>每条审计事件在入库时生成 SHA-256 签名，
 * 通过哈希链（previous_hash）确保不可篡改性。</p>
 */
public class AuditSignature {

    private String id;
    private String auditId;
    private String hash;
    private String previousHash;
    private String algorithm;
    private LocalDateTime createdAt;

    public AuditSignature() {
    }

    private AuditSignature(Builder builder) {
        this.id = builder.id;
        this.auditId = builder.auditId;
        this.hash = builder.hash;
        this.previousHash = builder.previousHash;
        this.algorithm = builder.algorithm;
        this.createdAt = builder.createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private String auditId;
        private String hash;
        private String previousHash;
        private String algorithm = "SHA-256";
        private LocalDateTime createdAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder auditId(String auditId) { this.auditId = auditId; return this; }
        public Builder hash(String hash) { this.hash = hash; return this; }
        public Builder previousHash(String previousHash) { this.previousHash = previousHash; return this; }
        public Builder algorithm(String algorithm) { this.algorithm = algorithm; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public AuditSignature build() {
            return new AuditSignature(this);
        }
    }

    // ======== Getters & Setters ========

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAuditId() { return auditId; }
    public void setAuditId(String auditId) { this.auditId = auditId; }
    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }
    public String getPreviousHash() { return previousHash; }
    public void setPreviousHash(String previousHash) { this.previousHash = previousHash; }
    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
