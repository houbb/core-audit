package com.github.houbb.core.audit.infrastructure.persistence.entity;

/**
 * 快照持久化实体
 * <p>直接映射 audit_snapshot 表字段。</p>
 */
public class AuditSnapshotEntity {

    private String id;
    private String auditId;
    private String snapshotType;
    private String contentJson;
    private String createdAt;
    private String createTime;
    private String updateTime;
    private String createUser;
    private String updateUser;

    // ======== Getters & Setters ========

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAuditId() { return auditId; }
    public void setAuditId(String auditId) { this.auditId = auditId; }
    public String getSnapshotType() { return snapshotType; }
    public void setSnapshotType(String snapshotType) { this.snapshotType = snapshotType; }
    public String getContentJson() { return contentJson; }
    public void setContentJson(String contentJson) { this.contentJson = contentJson; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
    public String getCreateUser() { return createUser; }
    public void setCreateUser(String createUser) { this.createUser = createUser; }
    public String getUpdateUser() { return updateUser; }
    public void setUpdateUser(String updateUser) { this.updateUser = updateUser; }
}
