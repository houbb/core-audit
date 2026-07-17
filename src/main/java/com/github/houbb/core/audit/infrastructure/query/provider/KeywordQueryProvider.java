package com.github.houbb.core.audit.infrastructure.query.provider;

import com.github.houbb.core.audit.application.domain.query.AuditQuery;
import com.github.houbb.core.audit.application.query.spi.AuditQueryProvider;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 全文关键字搜索 Provider（Keyword Scope）
 * <p>在 description、target_id、operator_name 三个字段中模糊匹配。</p>
 */
@Component
public class KeywordQueryProvider implements AuditQueryProvider {

    @Override
    public String getName() {
        return "Keyword";
    }

    @Override
    public int order() {
        return 400;
    }

    @Override
    public boolean supports(AuditQuery query) {
        return query.getKeyword() != null && !query.getKeyword().isBlank();
    }

    @Override
    public void contribute(StringBuilder sql, List<Object> params, AuditQuery query) {
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            sql.append(" AND (description LIKE ? OR target_id LIKE ? OR operator_name LIKE ?)");
            String kw = "%" + query.getKeyword() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
    }
}