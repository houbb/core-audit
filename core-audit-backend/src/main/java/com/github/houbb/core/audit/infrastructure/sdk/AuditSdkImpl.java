package com.github.houbb.core.audit.infrastructure.sdk;

import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.port.AuditSdkPort;
import com.github.houbb.core.audit.application.service.AuditEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Audit SDK 实现
 * <p>异步写入 + 静默失败：审计写入绝不阻塞业务，绝不因写入失败而中断业务。</p>
 *
 * <p>P9 增强：Sync / Async / Batch / BatchAsync 四种模式</p>
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

    /**
     * P9: 同步记录 — 阻塞等待结果
     */
    @Override
    public AuditEvent recordSync(AuditEvent event) {
        try {
            return auditEventService.record(event);
        } catch (Exception e) {
            log.error("Failed to sync record audit event: module={}, action={}",
                    event.getModule(), event.getAction(), e);
            return event; // return input on failure, fault isolation
        }
    }

    /**
     * P9: 批量同步记录
     */
    @Override
    public List<AuditEvent> recordBatch(List<AuditEvent> events) {
        List<AuditEvent> results = new ArrayList<>();
        if (events == null || events.isEmpty()) return results;
        for (AuditEvent event : events) {
            try {
                results.add(auditEventService.record(event));
            } catch (Exception e) {
                log.error("Failed to batch record audit event: {}", event.getTargetId(), e);
            }
        }
        return results;
    }

    /**
     * P9: 批量异步记录 — 不等待结果
     */
    @Override
    public void recordBatchAsync(List<AuditEvent> events) {
        if (events == null || events.isEmpty()) return;
        CompletableFuture.runAsync(() -> {
            for (AuditEvent event : events) {
                try {
                    auditEventService.record(event);
                } catch (Exception e) {
                    log.error("Failed to batch async record audit event: {}", event.getTargetId(), e);
                }
            }
        });
    }
}