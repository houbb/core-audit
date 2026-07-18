package com.github.houbb.core.audit.application.service;

import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.enums.AuditAction;
import com.github.houbb.core.audit.application.domain.enums.AuditEventType;
import com.github.houbb.core.audit.application.domain.enums.AuditModule;
import com.github.houbb.core.audit.application.domain.enums.AuditResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * P9 Enterprise SDK Tests
 */
class AuditSdkPortTest {

    @Test
    @DisplayName("P9: AuditEvent domain has tenant field with default value")
    void shouldHaveTenantField() {
        AuditEvent event = AuditEvent.builder()
                .module(AuditModule.USER)
                .action(AuditAction.CREATE)
                .eventType(AuditEventType.USER_CREATED)
                .targetType("User")
                .targetId("u-1")
                .operatorId("op-1")
                .operatorName("test")
                .result(AuditResult.SUCCESS)
                .description("test event")
                .build();

        // tenant defaults to null in builder, service sets "default"
        assertNull(event.getTenant());
        event.setTenant("tenant-a");
        assertEquals("tenant-a", event.getTenant());
    }

    @Test
    @DisplayName("P9: AuditEvent builder supports tenant")
    void shouldSetTenantViaBuilder() {
        AuditEvent event = AuditEvent.builder()
                .module(AuditModule.USER)
                .action(AuditAction.UPDATE)
                .targetType("User")
                .targetId("u-2")
                .operatorId("op-2")
                .operatorName("test")
                .result(AuditResult.SUCCESS)
                .description("tenant event")
                .tenant("enterprise-xyz")
                .build();

        assertEquals("enterprise-xyz", event.getTenant());
    }

    @Test
    @DisplayName("P9: Source field used for cross-system tracking")
    void shouldHaveSourceField() {
        AuditEvent event = AuditEvent.builder()
                .module(AuditModule.WORKFLOW)
                .action(AuditAction.EXECUTE)
                .targetType("Workflow")
                .targetId("wf-1")
                .operatorId("op-3")
                .operatorName("test")
                .result(AuditResult.SUCCESS)
                .description("cross-system")
                .source("core-workflow")
                .tenant("default")
                .build();

        assertEquals("core-workflow", event.getSource());
        assertEquals("default", event.getTenant());
    }
}