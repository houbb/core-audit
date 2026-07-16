package com.github.houbb.core.audit.application.domain;

import java.util.List;

/**
 * 审计事件分页结果
 */
public class AuditEventPage {

    /** 当前页审计事件列表 */
    private List<AuditEvent> items;

    /** 当前页码 */
    private int page;

    /** 每页条数 */
    private int size;

    /** 总条数 */
    private long total;

    /** 是否有下一页 */
    private boolean hasNext;

    public AuditEventPage() {
    }

    public AuditEventPage(List<AuditEvent> items, int page, int size, long total) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.total = total;
        this.hasNext = (long) page * size < total;
    }

    // ======== Getters ========

    public List<AuditEvent> getItems() { return items; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotal() { return total; }
    public boolean isHasNext() { return hasNext; }

    public void setItems(List<AuditEvent> items) { this.items = items; }
    public void setPage(int page) { this.page = page; }
    public void setSize(int size) { this.size = size; }
    public void setTotal(long total) { this.total = total; }
    public void setHasNext(boolean hasNext) { this.hasNext = hasNext; }
}