package com.github.houbb.core.audit.application.query.engine;

import com.github.houbb.core.audit.application.domain.query.AuditQuery;
import com.github.houbb.core.audit.application.query.spi.AuditQueryProvider;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 默认查询计划生成器（SQLite/MySQL 通用 SQL 方言）
 * <p>遍历所有 AuditQueryProvider 构建 WHERE 条件，处理 Diff JOIN，
 * 生成排序和分页子句。</p>
 */
@Component
public class DefaultQueryPlanner implements QueryPlanner {

    private final List<AuditQueryProvider> providers;

    public DefaultQueryPlanner(List<AuditQueryProvider> providers) {
        this.providers = providers != null
                ? providers.stream().sorted(Comparator.comparingInt(AuditQueryProvider::order)).toList()
                : List.of();
    }

    @Override
    public QueryPlan plan(AuditQuery query) {
        boolean hasDiff = query.hasDiffFilter();

        StringBuilder sql;
        StringBuilder countSql;
        List<Object> params;
        List<Object> countParams;

        if (hasDiff) {
            // Diff JOIN 模式
            sql = new StringBuilder("SELECT DISTINCT ae.* FROM audit_event ae INNER JOIN audit_change ac ON ae.id = ac.audit_id WHERE 1=1");
            countSql = new StringBuilder("SELECT COUNT(DISTINCT ae.id) FROM audit_event ae INNER JOIN audit_change ac ON ae.id = ac.audit_id WHERE 1=1");
        } else {
            sql = new StringBuilder("SELECT * FROM audit_event WHERE 1=1");
            countSql = new StringBuilder("SELECT COUNT(*) FROM audit_event WHERE 1=1");
        }

        params = new ArrayList<>();
        countParams = new ArrayList<>();

        // 遍历所有 Provider 追加 WHERE 条件
        for (AuditQueryProvider provider : providers) {
            if (provider.supports(query)) {
                provider.contribute(sql, params, query);
                provider.contribute(countSql, countParams, query);
            }
        }

        // 排序
        String orderBy = SortEngine.buildOrderBy(query.getSortField(), query.getSortDirection());
        sql.append(" ").append(orderBy);

        // 分页
        int offset = (query.getPage() - 1) * query.getSize();
        sql.append(" LIMIT ? OFFSET ?");
        params.add(query.getSize());
        params.add(offset);

        return new QueryPlan(sql.toString(), params, countSql.toString(), countParams, hasDiff);
    }
}