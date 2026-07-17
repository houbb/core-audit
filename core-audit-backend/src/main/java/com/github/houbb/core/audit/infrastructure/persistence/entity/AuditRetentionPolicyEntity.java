package com.github.houbb.core.audit.infrastructure.persistence.entity;

/**
 * audit_retention_policy 表持久化实体
 */
public class AuditRetentionPolicyEntity {

    private String id;
    private String module;
    private String action;
    private String retentionDays;
    private String archive;
    private String enabled;
    private String createTime;
    private String updateTime;
    private String createUser;
    private String updateUser;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getRetentionDays() { return retentionDays; }
    public void setRetentionDays(String retentionDays) { this.retentionDays = retentionDays; }
    public String getArchive() { return archive; }
    public void setArchive(String archive) { this.archive = archive; }
    public String getEnabled() { return enabled; }
    public void setEnabled(String enabled) { this.enabled = enabled; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
    public String getCreateUser() { return createUser; }
    public void setCreateUser(String createUser) { this.createUser = createUser; }
    public String getUpdateUser() { return updateUser; }
    public void setUpdateUser(String updateUser) { this.updateUser = updateUser; }
}