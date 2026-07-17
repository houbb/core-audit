package com.github.houbb.core.audit.application.event;

import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.enums.AuditEventType;

import java.util.Set;

/**
 * 审计事件订阅者接口
 * <p>其他模块实现此接口来订阅审计事件。</p>
 * <p>示例：</p>
 * <pre>
 * {@code
 * @Component
 * public class NotificationSubscriber implements AuditEventSubscriber {
 *     public String getName() { return "Notification"; }
 *     public Set<AuditEventType> subscribedTypes() { return Set.of(AuditEventType.LOGIN_FAILED); }
 *     public void onEvent(AuditEvent event) { /* send SMS *&#47; }
 * }
 * }
 * </pre>
 */
public interface AuditEventSubscriber {

    /**
     * 订阅者名称（用于日志和 API 展示）
     *
     * @return 可读名称
     */
    String getName();

    /**
     * 订阅的事件类型
     * <p>返回空集合表示订阅全部事件。</p>
     *
     * @return 事件类型集合
     */
    Set<AuditEventType> subscribedTypes();

    /**
     * 接收并处理事件
     *
     * @param event 审计事件
     */
    void onEvent(AuditEvent event);
}
