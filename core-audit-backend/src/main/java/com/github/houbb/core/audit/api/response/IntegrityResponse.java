package com.github.houbb.core.audit.api.response;

import com.github.houbb.core.audit.application.domain.compliance.AuditSignature;

import java.time.LocalDateTime;

/**
 * 完整性签名响应
 */
public class IntegrityResponse {

    private String auditId;
    private String hash;
    private String previousHash;
    private String algorithm;
    private boolean verified;
    private LocalDateTime createdAt;

    public static IntegrityResponse from(AuditSignature signature, boolean verified) {
        IntegrityResponse resp = new IntegrityResponse();
        resp.auditId = signature.getAuditId();
        resp.hash = signature.getHash();
        resp.previousHash = signature.getPreviousHash();
        resp.algorithm = signature.getAlgorithm();
        resp.verified = verified;
        resp.createdAt = signature.getCreatedAt();
        return resp;
    }

    public String getAuditId() { return auditId; }
    public String getHash() { return hash; }
    public String getPreviousHash() { return previousHash; }
    public String getAlgorithm() { return algorithm; }
    public boolean isVerified() { return verified; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}