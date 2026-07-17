package com.github.houbb.core.audit.infrastructure.persistence.entity;

/**
 * audit_export_task 表持久化实体
 */
public class AuditExportTaskEntity {

    private String id;
    private String queryJson;
    private String format;
    private String maskEnabled;
    private String includeDiff;
    private String includeTimeline;
    private String status;
    private String filePath;
    private String errorMessage;
    private String createdBy;
    private String createdAt;
    private String completedAt;
    private String createTime;
    private String updateTime;
    private String createUser;
    private String updateUser;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getQueryJson() { return queryJson; }
    public void setQueryJson(String queryJson) { this.queryJson = queryJson; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    public String getMaskEnabled() { return maskEnabled; }
    public void setMaskEnabled(String maskEnabled) { this.maskEnabled = maskEnabled; }
    public String getIncludeDiff() { return includeDiff; }
    public void setIncludeDiff(String includeDiff) { this.includeDiff = includeDiff; }
    public String getIncludeTimeline() { return includeTimeline; }
    public void setIncludeTimeline(String includeTimeline) { this.includeTimeline = includeTimeline; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getCompletedAt() { return completedAt; }
    public void setCompletedAt(String completedAt) { this.completedAt = completedAt; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
    public String getCreateUser() { return createUser; }
    public void setCreateUser(String createUser) { this.createUser = createUser; }
    public String getUpdateUser() { return updateUser; }
    public void setUpdateUser(String updateUser) { this.updateUser = updateUser; }
}