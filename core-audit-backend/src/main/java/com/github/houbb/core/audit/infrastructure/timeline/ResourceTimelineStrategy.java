package com.github.houbb.core.audit.infrastructure.timeline;

import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.timeline.TimelineKey;
import com.github.houbb.core.audit.application.timeline.TimelineStrategy;
import org.springframework.stereotype.Component;

/**
 * 资源/对象时间线策略
 * <p>依据 targetType + targetId 将同一对象的所有操作聚合为一条 OBJECT Timeline。</p>
 * <p>用于展示"对象生命周期"。</p>
 */
@Component
public class ResourceTimelineStrategy implements TimelineStrategy {

    @Override
    public boolean supports(AuditEvent event) {
        return event.getTargetType() != null && !event.getTargetType().isBlank()
                && event.getTargetId() != null && !event.getTargetId().isBlank();
    }

    @Override
    public TimelineKey resolve(AuditEvent event) {
        // Key 格式：targetType#targetId，e.g. "User#1001"
        String key = event.getTargetType() + "#" + event.getTargetId();
        return TimelineKey.of("OBJECT", key);
    }

    @Override
    public int order() {
        return 300;
    }
}
