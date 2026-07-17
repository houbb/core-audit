package com.github.houbb.core.audit.infrastructure.persistence.entity;

/**
 * 搜索历史持久化实体
 * <p>映射 audit_search_history 表。</p>
 */
public class AuditSearchHistoryEntity {

    private String id;
    private String userId;
    private String queryJson;   // AuditQuery DSL JSON
    private String searchedAt;
    private String createTime;
    private String updateTime;
    private String createUser;
    private String updateUser;

    // ======== Getters & Setters ========

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getQueryJson() { return queryJson; }
    public void setQueryJson(String queryJson) { this.queryJson = queryJson; }
    public String getSearchedAt() { return searchedAt; }
    public void setSearchedAt(String searchedAt) { this.searchedAt = searchedAt; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
    public String getCreateUser() { return createUser; }
    public void setCreateUser(String createUser) { this.createUser = createUser; }
    public String getUpdateUser() { return updateUser; }
    public void setUpdateUser(String updateUser) { this.updateUser = updateUser; }
}