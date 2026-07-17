package com.github.houbb.core.audit.infrastructure.event;

import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.enums.AuditEventType;
import com.github.houbb.core.audit.application.event.AuditEventSubscriber;
import com.github.houbb.core.audit.application.event.EventBus;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

/**
 * 内置日志订阅者 — 示例和调试用
 * <p>订阅全部事件，收到后打 debug 日志。</p>
 * <p>生产环境可以通过日志级别控制是否输出。</p>
 */
@Component
public class LoggingAuditEventSubscriber implements AuditEventSubscriber {

    private static final Logger log = LoggerFactory.getLogger(LoggingAuditEventSubscriber.class);

    private final EventBus eventBus;

    public LoggingAuditEventSubscriber(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @PostConstruct
    public void init() {
        eventBus.subscribe(this);
    }

    @Override
    public String getName() {
        return "LoggingSubscriber";
    }

    @Override
    public Set<AuditEventType> subscribedTypes() {
        // 空集合 = 监听全部事件
        return Collections.emptySet();
    }

    @Override
    public void onEvent(AuditEvent event) {
        log.debug("Audit event received: type={}, source={}, eventId={}, module={}, action={}, target={}:{}, result={}",
                event.getEventType(),
                event.getSource(),
                event.getEventId(),
                event.getModule(),
                event.getAction(),
                event.getTargetType(),
                event.getTargetId(),
                event.getResult());
    }
}
