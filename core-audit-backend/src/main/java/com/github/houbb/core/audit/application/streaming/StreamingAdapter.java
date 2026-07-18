package com.github.houbb.core.audit.application.streaming;

import com.github.houbb.core.audit.application.domain.AuditEvent;

import java.util.List;

/**
 * P9 Streaming Adapter 接口（MQ Adapter 抽象层）
 * <p>统一抽象消息队列/事件流能力。
 * MVP 使用本地 LocalEventBus，企业版可接入 Kafka/Pulsar/RabbitMQ。</p>
 *
 * <p>设计原则：</p>
 * <ul>
 *   <li>业务代码只依赖此接口，不依赖具体 MQ 实现</li>
 *   <li>MVP 提供 LocalStreamingAdapter（内存实现）</li>
 *   <li>企业版实现 KafkaStreamingAdapter 后直接替换 Bean</li>
 *   <li>不修改任何业务代码</li>
 * </ul>
 */
public interface StreamingAdapter {

    /**
     * 发布审计事件到流
     *
     * @param event 审计事件
     */
    void publish(AuditEvent event);

    /**
     * 批量发布
     *
     * @param events 审计事件列表
     */
    void publishBatch(List<AuditEvent> events);

    /**
     * 适配器名称（用于 Dashboard 展示）
     */
    String name();

    /**
     * 是否启用
     */
    boolean isEnabled();
}