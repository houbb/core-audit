package com.github.houbb.core.audit.api.response;

import com.github.houbb.core.audit.application.port.SearchHistoryRepository.PopularQueryRecord;

/**
 * 热门查询响应
 */
public class PopularQueryResponse {

    private String queryJson;
    private long count;

    public PopularQueryResponse() {
    }

    public static PopularQueryResponse from(PopularQueryRecord record) {
        PopularQueryResponse resp = new PopularQueryResponse();
        resp.queryJson = record.getQueryJson();
        resp.count = record.getCount();
        return resp;
    }

    // ======== Getters & Setters ========

    public String getQueryJson() { return queryJson; }
    public void setQueryJson(String queryJson) { this.queryJson = queryJson; }
    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }
}