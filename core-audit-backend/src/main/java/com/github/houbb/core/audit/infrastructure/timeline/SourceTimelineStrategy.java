package com.github.houbb.core.audit.infrastructure.timeline;

import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.timeline.TimelineKey;
import com.github.houbb.core.audit.application.timeline.TimelineStrategy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * P9 Cross-System Timeline Strategy
 * <p>按 Source 系统聚合 Timeline，支持跨系统操作链串联。</p>
 *
 * <p>例如：core-user → core-workflow → core-billing → core-ai
 * 这四步如果是同一个 business 请求链路，就会被此策略聚合。</p>
 *
 * <p>设计：按 source + tenant 组合生成 TimelineKey，
 * 同一个来源系统在同一个租户下的所有事件归入同一条 Timeline。</p>
 *
 * <p>order=600 排在 P5 5 个策略之后，作为企业级兜底策略。</p>
 */
@Component
@Order(600)
public class SourceTimelineStrategy implements TimelineStrategy {

    @Override
    public boolean supports(AuditEvent event) {
        return event.getSource() != null && !event.getSource().isBlank();
    }

    @Override
    public TimelineKey resolve(AuditEvent event) {
        String tenant = event.getTenant() != null ? event.getTenant() : "default";
        String key = tenant + ":" + event.getSource();
        return TimelineKey.of("WORKFLOW", key);
    }

    @Override
    public int order() {
        return 600;
    }
}