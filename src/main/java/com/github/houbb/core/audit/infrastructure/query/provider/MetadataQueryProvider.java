package com.github.houbb.core.audit.infrastructure.query.provider;

import com.github.houbb.core.audit.application.domain.query.AuditQuery;
import com.github.houbb.core.audit.application.query.spi.AuditQueryProvider;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Metadata JSON 过滤 Provider（Metadata Scope）
 * <p>在 metadata JSON 列中按 key-value 过滤。</p>
 */
@Component
public class MetadataQueryProvider implements AuditQueryProvider {

    private final boolean sqlite;

    public MetadataQueryProvider(DataSource dataSource) {
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
        return "Metadata";
    }

    @Override
    public int order() {
        return 500;
    }

    @Override
    public boolean supports(AuditQuery query) {
        return (query.getMetadataKey() != null && !query.getMetadataKey().isBlank())
                || (query.getMetadataValue() != null && !query.getMetadataValue().isBlank());
    }

    @Override
    public void contribute(StringBuilder sql, List<Object> params, AuditQuery query) {
        if (query.getMetadataKey() != null && !query.getMetadataKey().isBlank()) {
            String path = "$." + query.getMetadataKey();
            if (query.getMetadataValue() != null && !query.getMetadataValue().isBlank()) {
                sql.append(" AND ").append(jsonExtract("metadata", path)).append(" LIKE ?");
                params.add("%" + query.getMetadataValue() + "%");
            } else {
                sql.append(" AND ").append(jsonExtract("metadata", path)).append(" IS NOT NULL");
            }
        } else if (query.getMetadataValue() != null && !query.getMetadataValue().isBlank()) {
            // 只有 value 没有 key：全文搜索 metadata JSON 字符串
            sql.append(" AND metadata LIKE ?");
            params.add("%" + query.getMetadataValue() + "%");
        }
    }
}