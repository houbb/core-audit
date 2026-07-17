package com.github.houbb.core.audit.api.response;

import com.github.houbb.core.audit.application.port.SavedQueryRepository.SavedQueryRecord;

/**
 * 保存查询响应
 */
public class SavedQueryResponse {

    private String id;
    private String name;
    private String ownerId;
    private String queryJson;
    private boolean isPublic;
    private String createdAt;

    public SavedQueryResponse() {
    }

    public static SavedQueryResponse from(SavedQueryRecord record) {
        SavedQueryResponse resp = new SavedQueryResponse();
        resp.id = record.getId();
        resp.name = record.getName();
        resp.ownerId = record.getOwnerId();
        resp.queryJson = record.getQueryJson();
        resp.isPublic = record.isPublic();
        resp.createdAt = record.getCreatedAt();
        return resp;
    }

    // ======== Getters & Setters ========

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public String getQueryJson() { return queryJson; }
    public void setQueryJson(String queryJson) { this.queryJson = queryJson; }
    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}