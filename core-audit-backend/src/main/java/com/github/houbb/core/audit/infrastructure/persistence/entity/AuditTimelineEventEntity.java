package com.github.houbb.core.audit.infrastructure.persistence.entity;

/**
 * 时间线事件关联实体
 * <p>纯 POJO，无注解，所有字段为 String，对应 audit_timeline_event 表。</p>
 */
public class AuditTimelineEventEntity {

    private String id;
    private String timelineId;
    private String auditId;
    private String sequence;
    private String createTime;
    private String updateTime;
    private String createUser;
    private String updateUser;

    // ======== Getters & Setters ========

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTimelineId() { return timelineId; }
    public void setTimelineId(String timelineId) { this.timelineId = timelineId; }
    public String getAuditId() { return auditId; }
    public void setAuditId(String auditId) { this.auditId = auditId; }
    public String getSequence() { return sequence; }
    public void setSequence(String sequence) { this.sequence = sequence; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
    public String getCreateUser() { return createUser; }
    public void setCreateUser(String createUser) { this.createUser = createUser; }
    public String getUpdateUser() { return updateUser; }
    public void setUpdateUser(String updateUser) { this.updateUser = updateUser; }
}
