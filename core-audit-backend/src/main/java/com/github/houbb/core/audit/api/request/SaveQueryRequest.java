package com.github.houbb.core.audit.api.request;

/**
 * 保存查询请求 DTO
 */
public class SaveQueryRequest {

    private String name;
    private String ownerId;
    private AuditQueryRequest query;
    private boolean isPublic;

    // ======== Getters & Setters ========

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public AuditQueryRequest getQuery() { return query; }
    public void setQuery(AuditQueryRequest query) { this.query = query; }
    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }
}