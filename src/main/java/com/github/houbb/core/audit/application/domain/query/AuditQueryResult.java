package com.github.houbb.core.audit.application.domain.query;

import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.diff.Change;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 审计查询结果包装
 * <p>包含分页信息 + 可选的 Diff 变更数据。</p>
 */
public class AuditQueryResult {

    /** 审计事件列表 */
    private final List<AuditEvent> items;

    /** 当前页码 */
    private final int page;

    /** 每页条数 */
    private final int size;

    /** 总条数 */
    private final long total;

    /** 是否有下一页 */
    private final boolean hasNext;

    /** Diff 变更数据（按 audit_id 分组），仅当查询包含 Diff 过滤时有值 */
    private final Map<String, List<Change>> diffChanges;

    public AuditQueryResult(List<AuditEvent> items, int page, int size, long total,
                            Map<String, List<Change>> diffChanges) {
        this.items = items != null ? items : Collections.emptyList();
        this.page = page;
        this.size = size;
        this.total = total;
        this.hasNext = (long) page * size < total;
        this.diffChanges = diffChanges != null ? diffChanges : Collections.emptyMap();
    }

    public AuditQueryResult(List<AuditEvent> items, int page, int size, long total) {
        this(items, page, size, total, Collections.emptyMap());
    }

    // ======== Getters ========

    public List<AuditEvent> getItems() { return items; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotal() { return total; }
    public boolean isHasNext() { return hasNext; }
    public Map<String, List<Change>> getDiffChanges() { return diffChanges; }
}