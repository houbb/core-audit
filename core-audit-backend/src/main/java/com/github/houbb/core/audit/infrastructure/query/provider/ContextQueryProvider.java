package com.github.houbb.core.audit.infrastructure.query.provider;

import com.github.houbb.core.audit.application.domain.query.AuditQuery;
import com.github.houbb.core.audit.application.query.spi.AuditQueryProvider;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * 上下文过滤 Provider（Context Scope）
 * <p>处理 operator/tenant/department/role/ip/uri/requestMethod/browser/os/device/traceId 过滤。
 * 其中 tenant/department/role/browser/os/device 通过 json_extract 从 context_json 列提取。</p>
 */
@Component
public class ContextQueryProvider implements AuditQueryProvider {

    private final boolean sqlite;

    public ContextQueryProvider(DataSource dataSource) {
        this.sqlite = isSqlite(dataSource);
    }

    private static boolean isSqlite(DataSource ds) {
        try {
            if (ds != null) {
                try (Connection conn = ds.getConnection()) {
                    String url = conn.getMetaData().getURL();
                    return url != null && url.contains("sqlite");
                }
            }
        } catch (SQLException e) {
            // Fallback: assume not SQLite
        }
        return false;
    }

    private String jsonExtract(String column, String jsonPath) {
        if (sqlite) {
            return "json_extract(" + column + ", '" + jsonPath + "')";
        }
        return "JSON_EXTRACT(" + column + ", '" + jsonPath + "')";
    }

    @Override
    public String getName() {
        return "Context";
    }

    @Override
    public int order() {
        return 200;
    }

    @Override
    public boolean supports(AuditQuery query) {
        return (query.getOperator() != null && !query.getOperator().isBlank())
                || (query.getTenant() != null && !query.getTenant().isBlank())
                || (query.getDepartment() != null && !query.getDepartment().isBlank())
                || (query.getRole() != null && !query.getRole().isBlank())
                || (query.getIp() != null && !query.getIp().isBlank())
                || (query.getUri() != null && !query.getUri().isBlank())
                || (query.getRequestMethod() != null && !query.getRequestMethod().isBlank())
                || (query.getBrowser() != null && !query.getBrowser().isBlank())
                || (query.getOs() != null && !query.getOs().isBlank())
                || (query.getDevice() != null && !query.getDevice().isBlank())
                || (query.getTraceId() != null && !query.getTraceId().isBlank());
    }

    @Override
    public void contribute(StringBuilder sql, List<Object> params, AuditQuery query) {
        if (query.getOperator() != null && !query.getOperator().isBlank()) {
            sql.append(" AND operator_name LIKE ?");
            params.add("%" + query.getOperator() + "%");
        }
        if (query.getTenant() != null && !query.getTenant().isBlank()) {
            sql.append(" AND ").append(jsonExtract("context_json", "$.operator.tenant")).append(" = ?");
            params.add(query.getTenant());
        }
        if (query.getDepartment() != null && !query.getDepartment().isBlank()) {
            sql.append(" AND ").append(jsonExtract("context_json", "$.operator.department")).append(" = ?");
            params.add(query.getDepartment());
        }
        if (query.getRole() != null && !query.getRole().isBlank()) {
            sql.append(" AND ").append(jsonExtract("context_json", "$.operator.role")).append(" = ?");
            params.add(query.getRole());
        }
        if (query.getIp() != null && !query.getIp().isBlank()) {
            sql.append(" AND ").append(jsonExtract("context_json", "$.client.ip")).append(" = ?");
            params.add(query.getIp());
        }
        if (query.getUri() != null && !query.getUri().isBlank()) {
            sql.append(" AND request_uri LIKE ?");
            params.add("%" + query.getUri() + "%");
        }
        if (query.getRequestMethod() != null && !query.getRequestMethod().isBlank()) {
            sql.append(" AND request_method = ?");
            params.add(query.getRequestMethod().toUpperCase());
        }
        if (query.getBrowser() != null && !query.getBrowser().isBlank()) {
            sql.append(" AND ").append(jsonExtract("context_json", "$.client.browser")).append(" = ?");
            params.add(query.getBrowser());
        }
        if (query.getOs() != null && !query.getOs().isBlank()) {
            sql.append(" AND ").append(jsonExtract("context_json", "$.client.os")).append(" = ?");
            params.add(query.getOs());
        }
        if (query.getDevice() != null && !query.getDevice().isBlank()) {
            sql.append(" AND ").append(jsonExtract("context_json", "$.client.device")).append(" = ?");
            params.add(query.getDevice());
        }
        if (query.getTraceId() != null && !query.getTraceId().isBlank()) {
            sql.append(" AND trace_id = ?");
            params.add(query.getTraceId());
        }
    }
}