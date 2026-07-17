package com.github.houbb.core.audit.infrastructure.query.provider;

import com.github.houbb.core.audit.application.domain.query.AuditQuery;
import com.github.houbb.core.audit.application.query.spi.AuditQueryProvider;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Diff 过滤 Provider（Diff Scope）
 * <p>处理 diffField/diffBefore/diffAfter/diffType 过滤。
 * 注意：此 Provider 只追加 WHERE 条件（ac.xxx = ?），
 * INNER JOIN audit_change 由 DefaultQueryPlanner 统一处理。</p>
 */
@Component
public class DiffQueryProvider implements AuditQueryProvider {

    @Override
    public String getName() {
        return "Diff";
    }

    @Override
    public int order() {
        return 300;
    }

    @Override
    public boolean supports(AuditQuery query) {
        return query.hasDiffFilter();
    }

    @Override
    public void contribute(StringBuilder sql, List<Object> params, AuditQuery query) {
        if (query.getDiffField() != null && !query.getDiffField().isBlank()) {
            sql.append(" AND ac.field_name = ?");
            params.add(query.getDiffField());
        }
        if (query.getDiffBefore() != null && !query.getDiffBefore().isBlank()) {
            sql.append(" AND ac.before_value LIKE ?");
            params.add("%" + query.getDiffBefore() + "%");
        }
        if (query.getDiffAfter() != null && !query.getDiffAfter().isBlank()) {
            sql.append(" AND ac.after_value LIKE ?");
            params.add("%" + query.getDiffAfter() + "%");
        }
        if (query.getDiffType() != null) {
            sql.append(" AND ac.change_type = ?");
            params.add(query.getDiffType().name());
        }
    }
}