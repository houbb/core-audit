package com.github.houbb.core.audit.application.query.engine;

import com.github.houbb.core.audit.application.domain.enums.AuditAction;
import com.github.houbb.core.audit.application.domain.enums.AuditModule;
import com.github.houbb.core.audit.application.domain.enums.AuditResult;
import com.github.houbb.core.audit.application.domain.enums.ChangeType;
import com.github.houbb.core.audit.application.domain.query.AuditQuery;
import com.github.houbb.core.audit.application.query.spi.AuditQueryProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DefaultQueryPlanner 单元测试")
class DefaultQueryPlannerTest {

    @Test
    @DisplayName("空查询 — 无过滤条件时生成基础查询")
    void shouldGenerateBasicQueryWhenNoFilters() {
        DefaultQueryPlanner planner = new DefaultQueryPlanner(List.of());
        AuditQuery query = AuditQuery.builder().page(1).size(20).build();

        QueryPlan plan = planner.plan(query);

        assertNotNull(plan);
        assertTrue(plan.getSql().contains("SELECT * FROM audit_event WHERE 1=1"));
        assertTrue(plan.getSql().contains("ORDER BY created_at DESC"));
        assertTrue(plan.getSql().contains("LIMIT ? OFFSET ?"));
        assertEquals(20, plan.getParams().get(plan.getParams().size() - 2));
        assertEquals(0, plan.getParams().get(plan.getParams().size() - 1));
        assertFalse(plan.isHasDiffJoin());
    }

    @Test
    @DisplayName("基础过滤 — module + action 生成对应 WHERE 条件")
    void shouldGenerateBasicFilters() {
        DefaultQueryPlanner planner = new DefaultQueryPlanner(List.of(new TestBasicProvider()));
        AuditQuery query = AuditQuery.builder()
                .module(AuditModule.USER)
                .action(AuditAction.DELETE)
                .result(AuditResult.SUCCESS)
                .page(1).size(20)
                .build();

        QueryPlan plan = planner.plan(query);

        assertNotNull(plan);
        String sql = plan.getSql();
        assertTrue(sql.contains("module = ?"), "SQL should contain module filter");
        assertTrue(sql.contains("action = ?"), "SQL should contain action filter");
        assertTrue(sql.contains("result = ?"), "SQL should contain result filter");
        assertFalse(plan.isHasDiffJoin());
    }

    @Test
    @DisplayName("Diff JOIN — 有 Diff 过滤时自动 JOIN audit_change 表")
    void shouldGenerateDiffJoinWhenDiffFilterPresent() {
        DefaultQueryPlanner planner = new DefaultQueryPlanner(List.of(new TestDiffProvider()));
        AuditQuery query = AuditQuery.builder()
                .diffField("role")
                .diffType(ChangeType.UPDATE)
                .page(1).size(20)
                .build();

        QueryPlan plan = planner.plan(query);

        assertNotNull(plan);
        String sql = plan.getSql();
        assertTrue(sql.contains("INNER JOIN audit_change ac ON ae.id = ac.audit_id"),
                "SQL should contain INNER JOIN");
        assertTrue(sql.contains("SELECT DISTINCT ae.*"),
                "SQL should use SELECT DISTINCT");
        assertTrue(plan.isHasDiffJoin());
    }

    @Test
    @DisplayName("关键字搜索 — keyword 条件生成 LIKE 查询")
    void shouldGenerateKeywordFilter() {
        DefaultQueryPlanner planner = new DefaultQueryPlanner(List.of(new TestKeywordProvider()));
        AuditQuery query = AuditQuery.builder()
                .keyword("admin delete")
                .page(1).size(20)
                .build();

        QueryPlan plan = planner.plan(query);

        assertNotNull(plan);
        String sql = plan.getSql();
        assertTrue(sql.contains("description LIKE ?"), "SQL should contain description LIKE");
        assertTrue(sql.contains("target_id LIKE ?"), "SQL should contain target_id LIKE");
        assertTrue(sql.contains("operator_name LIKE ?"), "SQL should contain operator_name LIKE");
    }

    @Test
    @DisplayName("排序 — 自定义排序字段和方向")
    void shouldGenerateCustomSorting() {
        DefaultQueryPlanner planner = new DefaultQueryPlanner(List.of());
        AuditQuery query = AuditQuery.builder()
                .sortField("operator_name")
                .sortDirection("ASC")
                .page(1).size(10)
                .build();

        QueryPlan plan = planner.plan(query);

        assertNotNull(plan);
        assertTrue(plan.getSql().contains("ORDER BY operator_name ASC"));
    }

    @Test
    @DisplayName("排序 — 非法字段回退到默认排序")
    void shouldFallbackToDefaultSortForInvalidField() {
        DefaultQueryPlanner planner = new DefaultQueryPlanner(List.of());
        AuditQuery query = AuditQuery.builder()
                .sortField("nonexistent_field")
                .sortDirection("INVALID")
                .page(1).size(10)
                .build();

        QueryPlan plan = planner.plan(query);

        assertNotNull(plan);
        assertTrue(plan.getSql().contains("ORDER BY created_at DESC"));
    }

    @Test
    @DisplayName("分页 — 第二页 offset 计算正确")
    void shouldCalculateCorrectOffset() {
        DefaultQueryPlanner planner = new DefaultQueryPlanner(List.of());
        AuditQuery query = AuditQuery.builder().page(3).size(20).build();

        QueryPlan plan = planner.plan(query);

        assertEquals(40, plan.getParams().get(plan.getParams().size() - 1));
    }

    @Test
    @DisplayName("完整查询 — 多 Provider 组合（Basic + Keyword）")
    void shouldCombineMultipleProviders() {
        DefaultQueryPlanner planner = new DefaultQueryPlanner(
                List.of(new TestBasicProvider(), new TestKeywordProvider()));
        AuditQuery query = AuditQuery.builder()
                .module(AuditModule.CONFIG)
                .action(AuditAction.UPDATE)
                .operator("echo")
                .keyword("config")
                .startTime(LocalDateTime.of(2026, 7, 1, 0, 0))
                .endTime(LocalDateTime.of(2026, 7, 17, 23, 59))
                .sortField("created_at")
                .sortDirection("DESC")
                .page(1).size(30)
                .build();

        QueryPlan plan = planner.plan(query);

        assertNotNull(plan);
        String sql = plan.getSql();
        assertTrue(sql.contains("module = ?"));
        assertTrue(sql.contains("action = ?"));
        assertTrue(sql.contains("description LIKE ?"));
        assertFalse(plan.isHasDiffJoin());
    }

    // ======== 测试用简易 Providers ========

    static class TestBasicProvider implements AuditQueryProvider {
        @Override public String getName() { return "TestBasic"; }
        @Override public int order() { return 100; }
        @Override public boolean supports(AuditQuery query) {
            return query.getModule() != null || query.getAction() != null
                    || query.getResult() != null || query.getEventType() != null;
        }
        @Override public void contribute(StringBuilder sql, List<Object> params, AuditQuery query) {
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
        }
    }

    static class TestDiffProvider implements AuditQueryProvider {
        @Override public String getName() { return "TestDiff"; }
        @Override public int order() { return 300; }
        @Override public boolean supports(AuditQuery query) { return query.hasDiffFilter(); }
        @Override public void contribute(StringBuilder sql, List<Object> params, AuditQuery query) {
            if (query.getDiffField() != null) {
                sql.append(" AND ac.field_name = ?");
                params.add(query.getDiffField());
            }
            if (query.getDiffType() != null) {
                sql.append(" AND ac.change_type = ?");
                params.add(query.getDiffType().name());
            }
        }
    }

    static class TestKeywordProvider implements AuditQueryProvider {
        @Override public String getName() { return "TestKeyword"; }
        @Override public int order() { return 400; }
        @Override public boolean supports(AuditQuery query) {
            return query.getKeyword() != null && !query.getKeyword().isBlank();
        }
        @Override public void contribute(StringBuilder sql, List<Object> params, AuditQuery query) {
            sql.append(" AND (description LIKE ? OR target_id LIKE ? OR operator_name LIKE ?)");
            String kw = "%" + query.getKeyword() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
    }
}