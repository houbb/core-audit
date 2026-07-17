package com.github.houbb.core.audit.application.port;

import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.enums.AuditAction;
import com.github.houbb.core.audit.application.domain.enums.AuditEventType;
import com.github.houbb.core.audit.application.domain.enums.AuditModule;
import com.github.houbb.core.audit.application.domain.enums.AuditResult;

import java.util.HashMap;
import java.util.Map;

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

    /**
     * P3 便捷方法：带 Diff（自动比较 before/after）
     * <p>before/after 序列化为 JSON 存入 metadata._before / metadata._after。</p>
     * <p>DiffEngine 在 record() 流程中自动检出并生成结构化变更。</p>
     * <p>仅对 AuditAction.UPDATE 生效。</p>
     *
     * @param module        所属模块
     * @param action        操作类型（建议 UPDATE）
     * @param eventType     事件类型
     * @param targetType    目标对象类型
     * @param targetId      目标对象 ID
     * @param operatorId    操作人 ID
     * @param operatorName  操作人名称
     * @param result        执行结果
     * @param description   操作描述
     * @param before        变更前的对象
     * @param after         变更后的对象
     */
    default void record(AuditModule module, AuditAction action,
                        AuditEventType eventType,
                        String targetType, String targetId,
                        String operatorId, String operatorName,
                        AuditResult result, String description,
                        Object before, Object after) {
        Map<String, Object> metadata = new HashMap<>();
        if (before != null) {
            metadata.put("_before", before);
        }
        if (after != null) {
            metadata.put("_after", after);
        }
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
                .metadata(metadata)
                .build());
    }
}