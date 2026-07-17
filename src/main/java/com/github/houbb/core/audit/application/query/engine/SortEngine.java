package com.github.houbb.core.audit.application.query.engine;

/**
 * 排序引擎
 * <p>统一验证和构建 ORDER BY 子句。</p>
 */
public final class SortEngine {

    /** 允许排序的字段白名单 */
    private static final java.util.Set<String> ALLOWED_SORT_FIELDS = java.util.Set.of(
            "created_at", "operator_name", "module", "action"
    );

    private SortEngine() {
    }

    /**
     * 构建 ORDER BY 子句
     *
     * @param sortField     排序字段（可能为 null）
     * @param sortDirection 排序方向（可能为 null）
     * @return ORDER BY 子句，如 "ORDER BY created_at DESC"
     */
    public static String buildOrderBy(String sortField, String sortDirection) {
        String field = resolveField(sortField);
        String direction = resolveDirection(sortDirection);
        return "ORDER BY " + field + " " + direction;
    }

    private static String resolveField(String sortField) {
        if (sortField == null || sortField.isBlank()) {
            return "created_at";
        }
        String normalized = sortField.trim().toLowerCase();
        if (ALLOWED_SORT_FIELDS.contains(normalized)) {
            return normalized;
        }
        return "created_at"; // 非法字段回退到默认排序
    }

    private static String resolveDirection(String sortDirection) {
        if (sortDirection == null || sortDirection.isBlank()) {
            return "DESC";
        }
        String normalized = sortDirection.trim().toUpperCase();
        if ("ASC".equals(normalized) || "DESC".equals(normalized)) {
            return normalized;
        }
        return "DESC"; // 非法方向回退到 DESC
    }
}