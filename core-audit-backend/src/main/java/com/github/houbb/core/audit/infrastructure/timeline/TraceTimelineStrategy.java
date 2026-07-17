package com.github.houbb.core.audit.infrastructure.timeline;

import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.timeline.TimelineKey;
import com.github.houbb.core.audit.application.timeline.TimelineStrategy;
import org.springframework.stereotype.Component;

/**
 * 请求链路时间线策略
 * <p>依据 Trace ID 将同一分布式调用链上的所有事件聚合为一条 REQUEST Timeline。</p>
 */
@Component
public class TraceTimelineStrategy implements TimelineStrategy {

    @Override
    public boolean supports(AuditEvent event) {
        return event.getTraceId() != null && !event.getTraceId().isBlank();
    }

    @Override
    public TimelineKey resolve(AuditEvent event) {
        return TimelineKey.of("REQUEST", event.getTraceId());
    }

    @Override
    public int order() {
        return 100;
    }
}
