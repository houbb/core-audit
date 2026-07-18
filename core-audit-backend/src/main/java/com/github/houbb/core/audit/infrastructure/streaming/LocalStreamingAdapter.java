package com.github.houbb.core.audit.infrastructure.streaming;

import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.streaming.StreamingAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * P9 Local Streaming Adapter（MVP 实现）
 * <p>基于内存的本地事件总线，不依赖任何 MQ 中间件。</p>
 * <p>当没有其他 StreamingAdapter Bean 时自动启用。</p>
 *
 * <p>企业版替换方式：</p>
 * <pre>
 * &#64;Component
 * public class KafkaStreamingAdapter implements StreamingAdapter {
 *     // 实现 Kafka 连接逻辑
 * }
 * </pre>
 */
@Component
@ConditionalOnMissingBean(value = StreamingAdapter.class, ignored = LocalStreamingAdapter.class)
public class LocalStreamingAdapter implements StreamingAdapter {

    private static final Logger log = LoggerFactory.getLogger(LocalStreamingAdapter.class);

    private final List<Consumer<AuditEvent>> listeners = new CopyOnWriteArrayList<>();

    @Override
    public void publish(AuditEvent event) {
        if (event == null) return;
        for (Consumer<AuditEvent> listener : listeners) {
            try {
                listener.accept(event);
            } catch (Exception e) {
                log.warn("Streaming listener error: {}", e.getMessage());
            }
        }
    }

    @Override
    public void publishBatch(List<AuditEvent> events) {
        if (events == null || events.isEmpty()) return;
        for (AuditEvent event : events) {
            publish(event);
        }
    }

    @Override
    public String name() {
        return "LocalStreamingAdapter";
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * 注册流监听器
     */
    public void registerListener(Consumer<AuditEvent> listener) {
        listeners.add(listener);
        log.info("Streaming listener registered, total: {}", listeners.size());
    }

    /**
     * 移除流监听器
     */
    public void removeListener(Consumer<AuditEvent> listener) {
        listeners.remove(listener);
    }
}