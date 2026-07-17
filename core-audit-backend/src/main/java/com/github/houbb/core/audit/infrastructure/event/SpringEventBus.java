package com.github.houbb.core.audit.infrastructure.event;

import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.enums.AuditEventType;
import com.github.houbb.core.audit.application.event.AuditEventSubscriber;
import com.github.houbb.core.audit.application.event.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * EventBus 默认实现 — 基于 Spring ApplicationEventPublisher
 * <p>P1 使用内存 EventBus，所有订阅者在 JVM 内同步通知。</p>
 * <p>后续可通过替换此 Component 来接入 Kafka / RabbitMQ 等中间件。</p>
 */
@Component
public class SpringEventBus implements EventBus {

    private static final Logger log = LoggerFactory.getLogger(SpringEventBus.class);

    private final ApplicationEventPublisher springPublisher;
    private final List<AuditEventSubscriber> subscribers = new CopyOnWriteArrayList<>();

    public SpringEventBus(ApplicationEventPublisher springPublisher) {
        this.springPublisher = springPublisher;
    }

    @Override
    public void publish(AuditEvent event) {
        // 1. Publish to Spring's event system (for @EventListener beans)
        springPublisher.publishEvent(new AuditEventPublished(this, event));

        // 2. Fan-out to registered subscribers
        for (AuditEventSubscriber subscriber : subscribers) {
            if (!matches(subscriber, event.getEventType())) {
                continue;
            }
            try {
                subscriber.onEvent(event);
            } catch (Exception e) {
                log.error("Subscriber '{}' failed to handle event: eventId={}, eventType={}",
                        subscriber.getName(), event.getEventId(), event.getEventType(), e);
            }
        }
    }

    @Override
    public void subscribe(AuditEventSubscriber subscriber) {
        subscribers.add(subscriber);
        log.info("Subscriber registered: {} (listening: {})",
                subscriber.getName(),
                subscriber.subscribedTypes().isEmpty() ? "ALL" : subscriber.subscribedTypes());
    }

    @Override
    public void unsubscribe(AuditEventSubscriber subscriber) {
        subscribers.remove(subscriber);
        log.info("Subscriber unregistered: {}", subscriber.getName());
    }

    @Override
    public List<AuditEventSubscriber> getSubscribers() {
        return List.copyOf(subscribers);
    }

    private boolean matches(AuditEventSubscriber subscriber, AuditEventType eventType) {
        // Empty set means "listen to all"
        if (subscriber.subscribedTypes().isEmpty()) {
            return true;
        }
        return eventType != null && subscriber.subscribedTypes().contains(eventType);
    }
}
