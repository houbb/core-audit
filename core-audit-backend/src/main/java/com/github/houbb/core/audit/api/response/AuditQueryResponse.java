package com.github.houbb.core.audit.api.response;

import com.github.houbb.core.audit.application.domain.query.AuditQueryResult;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 审计查询响应
 * <p>包装分页结果 + Diff 变更数据。</p>
 */
public class AuditQueryResponse {

    private List<AuditEventResponse> items;
    private int page;
    private int size;
    private long total;
    private boolean hasNext;
    private Map<String, List<ChangeResponse>> diffChanges;

    public AuditQueryResponse() {
    }

    /**
     * 从 AuditQueryResult 构造
     */
    public static AuditQueryResponse from(AuditQueryResult result) {
        AuditQueryResponse resp = new AuditQueryResponse();
        resp.items = result.getItems().stream()
                .map(AuditEventResponse::from)
                .toList();
        resp.page = result.getPage();
        resp.size = result.getSize();
        resp.total = result.getTotal();
        resp.hasNext = result.isHasNext();

        // 转换 diffChanges
        if (!result.getDiffChanges().isEmpty()) {
            resp.diffChanges = new java.util.LinkedHashMap<>();
            for (Map.Entry<String, List<com.github.houbb.core.audit.application.domain.diff.Change>> entry
                    : result.getDiffChanges().entrySet()) {
                resp.diffChanges.put(entry.getKey(), entry.getValue().stream()
                        .map(ChangeResponse::from)
                        .toList());
            }
        } else {
            resp.diffChanges = Collections.emptyMap();
        }
        return resp;
    }

    // ======== Getters & Setters ========

    public List<AuditEventResponse> getItems() { return items; }
    public void setItems(List<AuditEventResponse> items) { this.items = items; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    public boolean isHasNext() { return hasNext; }
    public void setHasNext(boolean hasNext) { this.hasNext = hasNext; }
    public Map<String, List<ChangeResponse>> getDiffChanges() { return diffChanges; }
    public void setDiffChanges(Map<String, List<ChangeResponse>> diffChanges) { this.diffChanges = diffChanges; }
}