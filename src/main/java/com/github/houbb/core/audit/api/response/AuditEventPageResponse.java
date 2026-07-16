package com.github.houbb.core.audit.api.response;

import java.util.List;

/**
 * 审计事件分页响应
 * <p>符合 tech 规范的标准分页格式：{items, page, size, total, hasNext}</p>
 */
public class AuditEventPageResponse {

    private List<AuditEventResponse> items;
    private int page;
    private int size;
    private long total;
    private boolean hasNext;

    public AuditEventPageResponse() {
    }

    public AuditEventPageResponse(List<AuditEventResponse> items, int page, int size, long total, boolean hasNext) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.total = total;
        this.hasNext = hasNext;
    }

    // ======== Getters ========

    public List<AuditEventResponse> getItems() { return items; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotal() { return total; }
    public boolean isHasNext() { return hasNext; }
}