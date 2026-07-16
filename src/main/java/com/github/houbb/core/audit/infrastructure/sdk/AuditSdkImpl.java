package com.github.houbb.core.audit.infrastructure.sdk;

import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.port.AuditSdkPort;
import com.github.houbb.core.audit.application.service.AuditEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Audit SDK 实现
 * <p>异步写入 + 静默失败：审计写入绝不阻塞业务，绝不因写入失败而中断业务。</p>
 */
@Component
public class AuditSdkImpl implements AuditSdkPort {

    private static final Logger log = LoggerFactory.getLogger(AuditSdkImpl.class);

    private final AuditEventService auditEventService;

    public AuditSdkImpl(AuditEventService auditEventService) {
        this.auditEventService = auditEventService;
    }

    @Override
    public void record(AuditEvent event) {
        CompletableFuture.runAsync(() -> {
            try {
                auditEventService.record(event);
            } catch (Exception e) {
                log.error("Failed to record audit event: module={}, action={}, target={}:{}",
                        event.getModule(), event.getAction(),
                        event.getTargetType(), event.getTargetId(), e);
                // 静默失败 — 绝不抛给调用方
            }
        });
    }
}