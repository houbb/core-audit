package com.github.houbb.core.audit.infrastructure.query.provider;

import com.github.houbb.core.audit.application.domain.enums.AuditAction;
import com.github.houbb.core.audit.application.domain.enums.AuditModule;
import com.github.houbb.core.audit.application.domain.enums.AuditResult;
import com.github.houbb.core.audit.application.domain.query.AuditQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BasicQueryProvider 单元测试")
class BasicQueryProviderTest {

    private final BasicQueryProvider provider = new BasicQueryProvider();

    @Test
    @DisplayName("无过滤条件时 supports 返回 false")
    void shouldNotSupportEmptyQuery() {
        AuditQuery query = AuditQuery.builder().build();
        assertFalse(provider.supports(query));
    }

    @Test
    @DisplayName("有 module 条件时 supports 返回 true")
    void shouldSupportModuleFilter() {
        AuditQuery query = AuditQuery.builder().module(AuditModule.USER).build();
        assertTrue(provider.supports(query));
    }

    @Test
    @DisplayName("所有 Basic 字段正确追加到 SQL")
    void shouldContributeAllBasicFilters() {
        AuditQuery query = AuditQuery.builder()
                .module(AuditModule.USER)
                .action(AuditAction.DELETE)
                .result(AuditResult.SUCCESS)
                .targetType("User")
                .targetId("user-123")
                .build();

        StringBuilder sql = new StringBuilder("SELECT * FROM audit_event WHERE 1=1");
        List<Object> params = new ArrayList<>();

        provider.contribute(sql, params, query);

        String result = sql.toString();
        assertTrue(result.contains("module = ?"));
        assertTrue(result.contains("action = ?"));
        assertTrue(result.contains("result = ?"));
        assertTrue(result.contains("target_type = ?"));
        assertTrue(result.contains("target_id = ?"));
        assertEquals(5, params.size());
        assertEquals("USER", params.get(0));
        assertEquals("DELETE", params.get(1));
        assertEquals("SUCCESS", params.get(2));
        assertEquals("User", params.get(3));
        assertEquals("user-123", params.get(4));
    }

    @Test
    @DisplayName("部分过滤条件只追加非 null 字段")
    void shouldOnlyContributeNonNullFilters() {
        AuditQuery query = AuditQuery.builder()
                .module(AuditModule.CONFIG)
                .build();

        StringBuilder sql = new StringBuilder("SELECT * FROM audit_event WHERE 1=1");
        List<Object> params = new ArrayList<>();

        provider.contribute(sql, params, query);

        String result = sql.toString();
        assertTrue(result.contains("module = ?"));
        assertFalse(result.contains("action = ?"));
        assertEquals(1, params.size());
    }
}