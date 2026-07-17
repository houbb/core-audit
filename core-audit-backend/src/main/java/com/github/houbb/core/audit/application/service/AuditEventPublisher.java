package com.github.houbb.core.audit.application.service;

import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.event.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 审计事件发布器
 * <p>负责在数据库写入成功后发布事件到 EventBus。</p>
 * <p>核心原则：Record First, Event Second — 发布失败不影响已落库的记录。</p>
 */
@Component
public class AuditEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(AuditEventPublisher.class);

    private final EventBus eventBus;

    public AuditEventPublisher(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * 发布审计事件
     * <p>如果 event.publish 为 false，则跳过发布。</p>
     * <p>发布失败只打日志，不抛异常 — 不阻塞调用方。</p>
     *
     * @param event 已保存的审计事件
     */
    public void publish(AuditEvent event) {
        if (!Boolean.TRUE.equals(event.isPublish())) {
            log.debug("Event publishing disabled for eventId={}", event.getEventId());
            return;
        }

        try {
            eventBus.publish(event);
            event.setPublishTime(LocalDateTime.now());
            event.setPublishResult("SUCCESS");
            log.debug("Event published: eventId={}, eventType={}", event.getEventId(), event.getEventType());
        } catch (Exception e) {
            event.setPublishTime(LocalDateTime.now());
            event.setPublishResult("FAIL");
            log.error("Failed to publish event: eventId={}, eventType={}",
                    event.getEventId(), event.getEventType(), e);
            // Do NOT rethrow — publish failure must not affect the recorded audit
        }
    }
}
