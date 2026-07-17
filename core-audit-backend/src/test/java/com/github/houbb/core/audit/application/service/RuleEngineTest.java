package com.github.houbb.core.audit.application.service;

import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.enums.AuditAction;
import com.github.houbb.core.audit.application.domain.enums.AuditModule;
import com.github.houbb.core.audit.application.domain.enums.AuditResult;
import com.github.houbb.core.audit.application.domain.intelligence.RiskLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RuleEngine 单元测试
 */
class RuleEngineTest {

    private RuleEngine ruleEngine;

    @BeforeEach
    void setUp() {
        ruleEngine = new RuleEngine();
    }

    @Test
    @DisplayName("Rule 1: 凌晨 DELETE → HIGH (70分)")
    void shouldDetectMidnightDelete() {
        AuditEvent event = AuditEvent.builder()
                .module(AuditModule.USER)
                .action(AuditAction.DELETE)
                .occurredAt(LocalDateTime.now().withHour(3).withMinute(15))
                .result(AuditResult.SUCCESS)
                .build();

        RuleEngine.RuleResult result = ruleEngine.evaluate(event);

        assertNotNull(result);
        assertEquals(70, result.getScore());
        assertEquals(RiskLevel.HIGH, result.getLevel());
        assertEquals("midnight-delete", result.getRuleName());
        assertTrue(result.getReason().contains("凌晨"));
        assertTrue(result.getReason().contains("DELETE"));
    }

    @Test
    @DisplayName("Rule 1 不命中: 白天 DELETE")
    void shouldNotTriggerMidnightDelete() {
        AuditEvent event = AuditEvent.builder()
                .module(AuditModule.USER)
                .action(AuditAction.DELETE)
                .occurredAt(LocalDateTime.now().withHour(14).withMinute(0))
                .result(AuditResult.SUCCESS)
                .build();

        RuleEngine.RuleResult result = ruleEngine.evaluate(event);

        assertNull(result);
    }

    @Test
    @DisplayName("Rule 2: 批量删除 >100 → CRITICAL (95分)")
    void shouldDetectBulkDelete() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("count", 500);
        AuditEvent event = AuditEvent.builder()
                .module(AuditModule.USER)
                .action(AuditAction.DELETE)
                .metadata(metadata)
                .result(AuditResult.SUCCESS)
                .build();

        RuleEngine.RuleResult result = ruleEngine.evaluate(event);

        assertNotNull(result);
        assertEquals(95, result.getScore());
        assertEquals(RiskLevel.CRITICAL, result.getLevel());
        assertEquals("bulk-delete", result.getRuleName());
    }

    @Test
    @DisplayName("Rule 2: 批量删除 >10 → HIGH (60分)")
    void shouldDetectBulkDeleteMedium() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("count", 50);
        AuditEvent event = AuditEvent.builder()
                .module(AuditModule.USER)
                .action(AuditAction.DELETE)
                .metadata(metadata)
                .result(AuditResult.SUCCESS)
                .build();

        RuleEngine.RuleResult result = ruleEngine.evaluate(event);

        assertNotNull(result);
        assertEquals(60, result.getScore());
        assertEquals(RiskLevel.HIGH, result.getLevel());
    }

    @Test
    @DisplayName("Rule 3: Admin + 非工作时间登录 → MEDIUM (40分)")
    void shouldDetectOffHoursAdminLogin() {
        AuditEvent event = AuditEvent.builder()
                .module(AuditModule.USER)
                .action(AuditAction.LOGIN)
                .operatorName("admin")
                .occurredAt(LocalDateTime.now().withHour(22).withMinute(0))
                .result(AuditResult.SUCCESS)
                .build();

        RuleEngine.RuleResult result = ruleEngine.evaluate(event);

        assertNotNull(result);
        assertEquals(40, result.getScore());
        assertEquals(RiskLevel.MEDIUM, result.getLevel());
        assertEquals("off-hours-admin-login", result.getRuleName());
    }

    @Test
    @DisplayName("Rule 3 不命中: Admin + 工作时间登录")
    void shouldNotTriggerOffHoursAdminLoginDuringWork() {
        AuditEvent event = AuditEvent.builder()
                .module(AuditModule.USER)
                .action(AuditAction.LOGIN)
                .operatorName("admin")
                .occurredAt(LocalDateTime.now().withHour(10).withMinute(0))
                .result(AuditResult.SUCCESS)
                .build();

        RuleEngine.RuleResult result = ruleEngine.evaluate(event);

        assertNull(result);
    }

    @Test
    @DisplayName("常规操作（UPDATE）不触发任何规则")
    void shouldNotTriggerAnyRule() {
        AuditEvent event = AuditEvent.builder()
                .module(AuditModule.USER)
                .action(AuditAction.UPDATE)
                .operatorName("user1")
                .occurredAt(LocalDateTime.now().withHour(10).withMinute(0))
                .result(AuditResult.SUCCESS)
                .build();

        RuleEngine.RuleResult result = ruleEngine.evaluate(event);

        assertNull(result);
    }

    @Test
    @DisplayName("Rule 4: 敏感模块 + _firstAccess → MEDIUM (35分)")
    void shouldDetectSensitiveModuleFirstAccess() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("_firstAccess", "true");
        AuditEvent event = AuditEvent.builder()
                .module(AuditModule.CONFIG)
                .action(AuditAction.UPDATE)
                .metadata(metadata)
                .result(AuditResult.SUCCESS)
                .build();

        RuleEngine.RuleResult result = ruleEngine.evaluate(event);

        assertNotNull(result);
        assertEquals(35, result.getScore());
        assertEquals(RiskLevel.MEDIUM, result.getLevel());
    }
}