package com.github.houbb.core.audit.application.domain.diff;

import java.time.LocalDateTime;

/**
 * 快照 — 业务对象在某一时刻的完整 JSON 状态
 * <p>与 Change 分离存储，用于完整恢复和审计回放。</p>
 */
public class Snapshot {

    /** 快照 ID（UUID） */
    private String id;

    /** 关联的审计事件 ID */
    private String auditId;

    /** 快照类型：BEFORE / AFTER */
    private SnapshotType snapshotType;

    /** 完整对象 JSON */
    private String contentJson;

    /** 快照创建时间 */
    private LocalDateTime createdAt;

    // ======== Constructors ========

    private Snapshot(Builder builder) {
        this.id = builder.id;
        this.auditId = builder.auditId;
        this.snapshotType = builder.snapshotType;
        this.contentJson = builder.contentJson;
        this.createdAt = builder.createdAt;
    }

    // ======== Builder ========

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private String auditId;
        private SnapshotType snapshotType;
        private String contentJson;
        private LocalDateTime createdAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder auditId(String auditId) { this.auditId = auditId; return this; }
        public Builder snapshotType(SnapshotType snapshotType) { this.snapshotType = snapshotType; return this; }
        public Builder contentJson(String contentJson) { this.contentJson = contentJson; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Snapshot build() {
            return new Snapshot(this);
        }
    }

    // ======== Getters & Setters ========

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAuditId() { return auditId; }
    public void setAuditId(String auditId) { this.auditId = auditId; }
    public SnapshotType getSnapshotType() { return snapshotType; }
    public void setSnapshotType(SnapshotType snapshotType) { this.snapshotType = snapshotType; }
    public String getContentJson() { return contentJson; }
    public void setContentJson(String contentJson) { this.contentJson = contentJson; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}