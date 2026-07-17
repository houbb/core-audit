package com.github.houbb.core.audit.application.domain.intelligence;

import java.time.LocalDateTime;

/**
 * 审计风险评分
 * <p>每条 Audit Event 的风险评分，包含评分、等级、原因。</p>
 */
public class AuditRisk {

    private String id;
    private String auditId;
    private int riskScore;
    private RiskLevel riskLevel;
    private String reason;
    private String ruleName;
    private String aiAnalysis;
    private LocalDateTime analyzedAt;

    public AuditRisk() {
    }

    private AuditRisk(Builder builder) {
        this.id = builder.id;
        this.auditId = builder.auditId;
        this.riskScore = builder.riskScore;
        this.riskLevel = builder.riskLevel;
        this.reason = builder.reason;
        this.ruleName = builder.ruleName;
        this.aiAnalysis = builder.aiAnalysis;
        this.analyzedAt = builder.analyzedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private String auditId;
        private int riskScore;
        private RiskLevel riskLevel;
        private String reason;
        private String ruleName;
        private String aiAnalysis;
        private LocalDateTime analyzedAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder auditId(String auditId) { this.auditId = auditId; return this; }
        public Builder riskScore(int riskScore) { this.riskScore = riskScore; return this; }
        public Builder riskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; return this; }
        public Builder reason(String reason) { this.reason = reason; return this; }
        public Builder ruleName(String ruleName) { this.ruleName = ruleName; return this; }
        public Builder aiAnalysis(String aiAnalysis) { this.aiAnalysis = aiAnalysis; return this; }
        public Builder analyzedAt(LocalDateTime analyzedAt) { this.analyzedAt = analyzedAt; return this; }

        public AuditRisk build() {
            if (riskLevel == null) {
                riskLevel = RiskLevel.fromScore(riskScore);
            }
            return new AuditRisk(this);
        }
    }

    // ======== Getters & Setters ========

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAuditId() { return auditId; }
    public void setAuditId(String auditId) { this.auditId = auditId; }
    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }
    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    public String getAiAnalysis() { return aiAnalysis; }
    public void setAiAnalysis(String aiAnalysis) { this.aiAnalysis = aiAnalysis; }
    public LocalDateTime getAnalyzedAt() { return analyzedAt; }
    public void setAnalyzedAt(LocalDateTime analyzedAt) { this.analyzedAt = analyzedAt; }
}