package com.github.houbb.core.audit.infrastructure.timeline;

import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.timeline.TimelineKey;
import com.github.houbb.core.audit.application.timeline.TimelineStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 会话时间线策略
 * <p>依据 Session ID 将同一次会话内的所有事件聚合为一条 SESSION Timeline。</p>
 * <p>Session ID 来源：HTTP Header X-Session-Id → RequestContext.sessionId。</p>
 */
@Component
public class SessionTimelineStrategy implements TimelineStrategy {

    private static final Logger log = LoggerFactory.getLogger(SessionTimelineStrategy.class);

    @Override
    public boolean supports(AuditEvent event) {
        if (event.getContext() == null || event.getContext().getRequest() == null) {
            return false;
        }
        String sessionId = event.getContext().getRequest().getSessionId();
        return sessionId != null && !sessionId.isBlank();
    }

    @Override
    public TimelineKey resolve(AuditEvent event) {
        String sessionId = event.getContext().getRequest().getSessionId();
        return TimelineKey.of("SESSION", sessionId);
    }

    @Override
    public int order() {
        return 200;
    }
}
