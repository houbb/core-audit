package com.github.houbb.core.audit.application.query.engine;

import java.util.Collections;
import java.util.List;

/**
 * 查询计划
 * <p>由 QueryPlanner 生成，包含完整的 SQL 和参数供 Repository 执行。</p>
 */
public class QueryPlan {

    /** 主查询 SQL */
    private final String sql;

    /** 主查询参数 */
    private final List<Object> params;

    /** COUNT 查询 SQL */
    private final String countSql;

    /** COUNT 查询参数 */
    private final List<Object> countParams;

    /** 是否使用了 Diff JOIN */
    private final boolean hasDiffJoin;

    public QueryPlan(String sql, List<Object> params, String countSql, List<Object> countParams, boolean hasDiffJoin) {
        this.sql = sql;
        this.params = params != null ? params : Collections.emptyList();
        this.countSql = countSql;
        this.countParams = countParams != null ? countParams : Collections.emptyList();
        this.hasDiffJoin = hasDiffJoin;
    }

    // ======== Getters ========

    public String getSql() { return sql; }
    public List<Object> getParams() { return params; }
    public String getCountSql() { return countSql; }
    public List<Object> getCountParams() { return countParams; }
    public boolean isHasDiffJoin() { return hasDiffJoin; }
}