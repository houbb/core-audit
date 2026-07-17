package com.github.houbb.core.audit.application.event;

import com.github.houbb.core.audit.application.domain.AuditEvent;

import java.util.List;

/**
 * 事件总线接口（Application 层端口）
 * <p>所有模块依赖此接口，而不是直接依赖 Spring Event 或具体 MQ。</p>
 * <p>P1 默认使用 Spring ApplicationEvent 实现（SpringEventBus），</p>
 * <p>后续可替换为 Kafka / RabbitMQ / RocketMQ 而不影响调用方。</p>
 */
public interface EventBus {

    /**
     * 发布一个审计事件到所有匹配的订阅者
     *
     * @param event 审计事件
     */
    void publish(AuditEvent event);

    /**
     * 注册一个订阅者
     *
     * @param subscriber 订阅者
     */
    void subscribe(AuditEventSubscriber subscriber);

    /**
     * 取消注册一个订阅者
     *
     * @param subscriber 订阅者
     */
    void unsubscribe(AuditEventSubscriber subscriber);

    /**
     * 获取所有已注册的订阅者（用于 API 展示）
     *
     * @return 订阅者列表快照
     */
    List<AuditEventSubscriber> getSubscribers();
}
