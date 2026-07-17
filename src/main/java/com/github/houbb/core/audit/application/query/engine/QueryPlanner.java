package com.github.houbb.core.audit.application.query.engine;

import com.github.houbb.core.audit.application.domain.query.AuditQuery;

/**
 * 查询计划生成器
 * <p>输入 AuditQuery DSL，输出 QueryPlan（SQL + 参数）。
 * 不同数据库（SQLite/MySQL/Elastic）可实现不同 Planner。</p>
 */
public interface QueryPlanner {

    /**
     * 生成查询计划
     *
     * @param query 统一查询 DSL
     * @return 查询计划（SQL + 参数）
     */
    QueryPlan plan(AuditQuery query);
}