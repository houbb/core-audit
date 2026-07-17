package com.github.houbb.core.audit.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.houbb.core.audit.application.domain.diff.Snapshot;
import com.github.houbb.core.audit.application.domain.diff.SnapshotType;
import com.github.houbb.core.audit.application.port.SnapshotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 快照解析器 — 捕获业务对象在变更前后的完整 JSON 状态
 * <p>负责序列化对象并保存到 audit_snapshot 表。</p>
 * <p>序列化失败不抛异常，只打日志。</p>
 */
@Component
public class SnapshotResolver {

    private static final Logger log = LoggerFactory.getLogger(SnapshotResolver.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final SnapshotRepository snapshotRepository;

    public SnapshotResolver(SnapshotRepository snapshotRepository) {
        this.snapshotRepository = snapshotRepository;
    }

    /**
     * 捕获变更前的快照
     *
     * @param auditId 关联的审计事件 ID
     * @param entity  业务对象
     * @return 快照（序列化失败时返回 null）
     */
    public Snapshot captureBefore(String auditId, Object entity) {
        return capture(auditId, entity, SnapshotType.BEFORE);
    }

    /**
     * 捕获变更后的快照
     *
     * @param auditId 关联的审计事件 ID
     * @param entity  业务对象
     * @return 快照（序列化失败时返回 null）
     */
    public Snapshot captureAfter(String auditId, Object entity) {
        return capture(auditId, entity, SnapshotType.AFTER);
    }

    private Snapshot capture(String auditId, Object entity, SnapshotType type) {
        if (entity == null) {
            log.debug("Skipping {} snapshot for audit {}: entity is null", type, auditId);
            return null;
        }

        try {
            String json = objectMapper.writeValueAsString(entity);

            Snapshot snapshot = Snapshot.builder()
                    .id(UUID.randomUUID().toString())
                    .auditId(auditId)
                    .snapshotType(type)
                    .contentJson(json)
                    .createdAt(LocalDateTime.now())
                    .build();

            return snapshotRepository.save(snapshot);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize entity for {} snapshot, auditId={}: {}",
                    type, auditId, e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Failed to save {} snapshot for auditId={}: {}",
                    type, auditId, e.getMessage());
            return null;
        }
    }
}
