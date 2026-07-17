package com.github.houbb.core.audit.infrastructure.query.provider;

import com.github.houbb.core.audit.application.domain.query.AuditQuery;
import com.github.houbb.core.audit.application.query.spi.AuditQueryProvider;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 基础过滤 Provider（Basic Scope）
 * <p>处理 module/action/result/eventType/targetType/targetId 过滤。</p>
 */
@Component
public class BasicQueryProvider implements AuditQueryProvider {

    @Override
    public String getName() {
        return "Basic";
    }

    @Override
    public int order() {
        return 100;
    }

    @Override
    public boolean supports(AuditQuery query) {
        return query.getModule() != null
                || query.getAction() != null
                || query.getResult() != null
                || query.getEventType() != null
                || (query.getTargetType() != null && !query.getTargetType().isBlank())
                || (query.getTargetId() != null && !query.getTargetId().isBlank());
    }

    @Override
    public void contribute(StringBuilder sql, List<Object> params, AuditQuery query) {
        if (query.getModule() != null) {
            sql.append(" AND module = ?");
            params.add(query.getModule().name());
        }
        if (query.getAction() != null) {
            sql.append(" AND action = ?");
            params.add(query.getAction().name());
        }
        if (query.getResult() != null) {
            sql.append(" AND result = ?");
            params.add(query.getResult().name());
        }
        if (query.getEventType() != null) {
            sql.append(" AND event_type = ?");
            params.add(query.getEventType().name());
        }
        if (query.getTargetType() != null && !query.getTargetType().isBlank()) {
            sql.append(" AND target_type = ?");
            params.add(query.getTargetType());
        }
        if (query.getTargetId() != null && !query.getTargetId().isBlank()) {
            sql.append(" AND target_id = ?");
            params.add(query.getTargetId());
        }
    }
}