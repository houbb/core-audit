package com.github.houbb.core.audit.infrastructure.timeline;

import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.timeline.TimelineKey;
import com.github.houbb.core.audit.application.timeline.TimelineStrategy;
import org.springframework.stereotype.Component;

/**
 * 操作人时间线策略
 * <p>依据 operatorId 将同一操作人的所有行为聚合为一条 USER Timeline。</p>
 * <p>用于展示"谁做了什么"。</p>
 */
@Component
public class OperatorTimelineStrategy implements TimelineStrategy {

    @Override
    public boolean supports(AuditEvent event) {
        return event.getOperatorId() != null && !event.getOperatorId().isBlank();
    }

    @Override
    public TimelineKey resolve(AuditEvent event) {
        return TimelineKey.of("USER", event.getOperatorId());
    }

    @Override
    public int order() {
        return 400;
    }
}
