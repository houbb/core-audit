package com.github.houbb.core.audit.application.domain.intelligence;

/**
 * 智能洞察
 * <p>AI 或规则引擎生成的洞察发现，包含标题、严重程度、摘要、建议和证据引用。</p>
 */
public class AuditInsight {

    private String id;
    private String auditId;      // 关联的 Audit Event（可为空，表示全局 Insight）
    private String title;
    private RiskLevel severity;
    private String summary;
    private String suggestion;
    private String evidenceJson;  // Evidence JSON（引用 Timeline/Replay/Diff/Audit IDs）
    private String agentName;     // 生成此 Insight 的 Agent 名称

    public AuditInsight() {
    }

    private AuditInsight(Builder builder) {
        this.id = builder.id;
        this.auditId = builder.auditId;
        this.title = builder.title;
        this.severity = builder.severity;
        this.summary = builder.summary;
        this.suggestion = builder.suggestion;
        this.evidenceJson = builder.evidenceJson;
        this.agentName = builder.agentName;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private String auditId;
        private String title;
        private RiskLevel severity;
        private String summary;
        private String suggestion;
        private String evidenceJson;
        private String agentName;

        public Builder id(String id) { this.id = id; return this; }
        public Builder auditId(String auditId) { this.auditId = auditId; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder severity(RiskLevel severity) { this.severity = severity; return this; }
        public Builder summary(String summary) { this.summary = summary; return this; }
        public Builder suggestion(String suggestion) { this.suggestion = suggestion; return this; }
        public Builder evidenceJson(String evidenceJson) { this.evidenceJson = evidenceJson; return this; }
        public Builder agentName(String agentName) { this.agentName = agentName; return this; }

        public AuditInsight build() {
            return new AuditInsight(this);
        }
    }

    // ======== Getters & Setters ========

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAuditId() { return auditId; }
    public void setAuditId(String auditId) { this.auditId = auditId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public RiskLevel getSeverity() { return severity; }
    public void setSeverity(RiskLevel severity) { this.severity = severity; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getSuggestion() { return suggestion; }
    public void setSuggestion(String suggestion) { this.suggestion = suggestion; }
    public String getEvidenceJson() { return evidenceJson; }
    public void setEvidenceJson(String evidenceJson) { this.evidenceJson = evidenceJson; }
    public String getAgentName() { return agentName; }
    public void setAgentName(String agentName) { this.agentName = agentName; }
}