package com.github.houbb.core.audit.application.port;

import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.enums.AuditAction;
import com.github.houbb.core.audit.application.domain.enums.AuditEventType;
import com.github.houbb.core.audit.application.domain.enums.AuditModule;
import com.github.houbb.core.audit.application.domain.enums.AuditResult;

/**
 * Audit SDK 对外接口
 * <p>所有 Core 模块统一通过此接口写入审计记录。</p>
 */
public interface AuditSdkPort {

    /**
     * 记录一条审计事件
     * <p>异步执行，不阻塞业务线程。写入失败只打日志，不抛异常。</p>
     *
     * @param event 审计事件
     */
    void record(AuditEvent event);

    /**
     * 便捷方法：Builder 模式一键记录（P0 兼容，不带 eventType）
     */
    default void record(AuditModule module, AuditAction action,
                        String targetType, String targetId,
                        String operatorId, String operatorName,
                        AuditResult result, String description) {
        record(AuditEvent.builder()
                .module(module)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .operatorId(operatorId)
                .operatorName(operatorName)
                .result(result)
                .description(description)
                .source(module.name())
                .build());
    }

    /**
     * 便捷方法：带 eventType（P1 新增 — 推荐使用）
     */
    default void record(AuditModule module, AuditAction action,
                        AuditEventType eventType,
                        String targetType, String targetId,
                        String operatorId, String operatorName,
                        AuditResult result, String description) {
        record(AuditEvent.builder()
                .module(module)
                .action(action)
                .eventType(eventType)
                .targetType(targetType)
                .targetId(targetId)
                .operatorId(operatorId)
                .operatorName(operatorName)
                .result(result)
                .description(description)
                .source(module.name())
                .publish(true)
                .build());
    }
}