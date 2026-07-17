package com.github.houbb.core.audit.application.domain.intelligence;

/**
 * 行为模式
 * <p>记录操作人/资源/工作流的行为模式，用于异常检测。
 * Pattern 类型包括：OPERATOR、RESOURCE、WORKFLOW、API、TENANT</p>
 */
public class AuditPattern {

    private String id;
    private String type;          // OPERATOR / RESOURCE / WORKFLOW / API / TENANT
    private String owner;         // 模式归属者（如操作人 ID）
    private String contentJson;   // Pattern JSON（行为模式数据）
    private double confidence;    // 置信度 0.0-1.0
    private int sampleCount;      // 样本数量

    public AuditPattern() {
    }

    private AuditPattern(Builder builder) {
        this.id = builder.id;
        this.type = builder.type;
        this.owner = builder.owner;
        this.contentJson = builder.contentJson;
        this.confidence = builder.confidence;
        this.sampleCount = builder.sampleCount;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private String type;
        private String owner;
        private String contentJson;
        private double confidence;
        private int sampleCount;

        public Builder id(String id) { this.id = id; return this; }
        public Builder type(String type) { this.type = type; return this; }
        public Builder owner(String owner) { this.owner = owner; return this; }
        public Builder contentJson(String contentJson) { this.contentJson = contentJson; return this; }
        public Builder confidence(double confidence) { this.confidence = confidence; return this; }
        public Builder sampleCount(int sampleCount) { this.sampleCount = sampleCount; return this; }

        public AuditPattern build() {
            return new AuditPattern(this);
        }
    }

    // ======== Getters & Setters ========

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
    public String getContentJson() { return contentJson; }
    public void setContentJson(String contentJson) { this.contentJson = contentJson; }
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    public int getSampleCount() { return sampleCount; }
    public void setSampleCount(int sampleCount) { this.sampleCount = sampleCount; }
}