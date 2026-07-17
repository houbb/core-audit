package com.github.houbb.core.audit.api.response;

import com.github.houbb.core.audit.application.port.SearchHistoryRepository.SearchHistoryRecord;

/**
 * 最近搜索响应
 */
public class RecentSearchResponse {

    private String id;
    private String userId;
    private String queryJson;
    private String searchedAt;

    public RecentSearchResponse() {
    }

    public static RecentSearchResponse from(SearchHistoryRecord record) {
        RecentSearchResponse resp = new RecentSearchResponse();
        resp.id = record.getId();
        resp.userId = record.getUserId();
        resp.queryJson = record.getQueryJson();
        resp.searchedAt = record.getSearchedAt();
        return resp;
    }

    // ======== Getters & Setters ========

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getQueryJson() { return queryJson; }
    public void setQueryJson(String queryJson) { this.queryJson = queryJson; }
    public String getSearchedAt() { return searchedAt; }
    public void setSearchedAt(String searchedAt) { this.searchedAt = searchedAt; }
}