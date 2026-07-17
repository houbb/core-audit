package com.github.houbb.core.audit.infrastructure.event;

import com.github.houbb.core.audit.application.domain.AuditEvent;
import org.springframework.context.ApplicationEvent;

/**
 * Spring ApplicationEvent 包装 Aud itEvent
 * <p>供外部模块使用 @EventListener 监听审计事件。</p>
 * <p>使用方式：</p>
 * <pre>
 * {@code
 * @EventListener
 * public void onAuditEvent(AuditEventPublished event) {
 *     AuditEvent audit = event.getAuditEvent();
 *     // handle event
 * }
 * }
 * </pre>
 */
public class AuditEventPublished extends ApplicationEvent {

    private final AuditEvent auditEvent;

    public AuditEventPublished(Object source, AuditEvent auditEvent) {
        super(source);
        this.auditEvent = auditEvent;
    }

    public AuditEvent getAuditEvent() {
        return auditEvent;
    }
}
