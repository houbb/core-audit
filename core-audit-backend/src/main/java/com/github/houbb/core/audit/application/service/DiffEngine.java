package com.github.houbb.core.audit.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.houbb.core.audit.application.diff.DiffStrategy;
import com.github.houbb.core.audit.application.domain.diff.ChangeSet;
import com.github.houbb.core.audit.infrastructure.config.AuditProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Diff 引擎 — 编排 DiffStrategy 的执行
 * <p>根据对象类型自动选择策略，处理大小限制和回退逻辑。</p>
 * <p>Diff 失败不阻塞审计记录写入。</p>
 */
@Component
public class DiffEngine {

    private static final Logger log = LoggerFactory.getLogger(DiffEngine.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final List<DiffStrategy<?>> strategies;
    private final AuditProperties properties;

    public DiffEngine(List<DiffStrategy<?>> strategies, AuditProperties properties) {
        this.strategies = strategies != null ? List.copyOf(strategies) : Collections.emptyList();
        this.properties = properties;
        log.info("DiffEngine initialized with {} strategy(s)", this.strategies.size());
    }

    /**
     * 比较两个对象并生成 ChangeSet
     *
     * @param targetType 目标对象类型名
     * @param targetId   目标对象 ID
     * @param before     变更前的对象
     * @param after      变更后的对象
     * @param operator   操作人
     * @return 结构化的变更集
     */
    public ChangeSet diff(String targetType, String targetId,
                          Object before, Object after, String operator) {

        // 如果禁用 diff，返回空结果
        if (properties.getDiff() != null && !properties.getDiff().isEnabled()) {
            log.debug("Diff is disabled via configuration");
            return ChangeSet.builder()
                    .targetType(targetType)
                    .targetId(targetId)
                    .operator(operator)
                    .time(LocalDateTime.now())
                    .changes(Collections.emptyList())
                    .build();
        }

        // 检查大小限制
        long maxSize = getMaxSnapshotSize();
        if (before != null && estimateSize(before) > maxSize) {
            log.warn("Before object exceeds max size {}, skipping diff for {}/{}", maxSize, targetType, targetId);
            return ChangeSet.builder()
                    .targetType(targetType)
                    .targetId(targetId)
                    .operator(operator)
                    .time(LocalDateTime.now())
                    .changes(Collections.emptyList())
                    .build();
        }
        if (after != null && estimateSize(after) > maxSize) {
            log.warn("After object exceeds max size {}, skipping diff for {}/{}", maxSize, targetType, targetId);
            return ChangeSet.builder()
                    .targetType(targetType)
                    .targetId(targetId)
                    .operator(operator)
                    .time(LocalDateTime.now())
                    .changes(Collections.emptyList())
                    .build();
        }

        Class<?> type = determineType(before, after);

        try {
            DiffStrategy<?> strategy = findStrategy(type);
            if (strategy == null) {
                log.debug("No matching DiffStrategy for type '{}', using BeanDiffStrategy as fallback", type.getName());
                // fallback: serialize to JSON and use JsonNode comparison
                JsonNode beforeNode = before != null ? objectMapper.valueToTree(before) : null;
                JsonNode afterNode = after != null ? objectMapper.valueToTree(after) : null;
                DiffStrategy<JsonNode> jsonStrategy = findJsonStrategy();
                if (jsonStrategy != null) {
                    return jsonStrategy.compare(beforeNode, afterNode);
                }
            } else {
                @SuppressWarnings("unchecked")
                DiffStrategy<Object> typedStrategy = (DiffStrategy<Object>) strategy;
                ChangeSet result = typedStrategy.compare(before, after);

                return ChangeSet.builder()
                        .targetType(targetType)
                        .targetId(targetId)
                        .operator(operator)
                        .time(LocalDateTime.now())
                        .changes(result.getChanges())
                        .build();
            }
        } catch (Exception e) {
            log.error("Diff failed for {}/{}: {}", targetType, targetId, e.getMessage());
        }

        return ChangeSet.builder()
                .targetType(targetType)
                .targetId(targetId)
                .operator(operator)
                .time(LocalDateTime.now())
                .changes(Collections.emptyList())
                .build();
    }

    private Class<?> determineType(Object before, Object after) {
        if (after != null) return after.getClass();
        if (before != null) return before.getClass();
        return Object.class;
    }

    private DiffStrategy<?> findStrategy(Class<?> type) {
        for (DiffStrategy<?> strategy : strategies) {
            if (strategy.supports(type)) {
                return strategy;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private DiffStrategy<JsonNode> findJsonStrategy() {
        return strategies.stream()
                .filter(s -> s.supports(JsonNode.class))
                .map(s -> (DiffStrategy<JsonNode>) s)
                .findFirst()
                .orElse(null);
    }

    private long estimateSize(Object obj) {
        try {
            String json = objectMapper.writeValueAsString(obj);
            return json.length();
        } catch (Exception e) {
            return 0;
        }
    }

    private long getMaxSnapshotSize() {
        if (properties.getDiff() != null && properties.getDiff().getMaxSnapshotSize() != null) {
            return properties.getDiff().getMaxSnapshotSize();
        }
        return 2 * 1024 * 1024; // 2MB default
    }
}
