package com.github.houbb.core.audit.application.port;

import com.github.houbb.core.audit.application.domain.enterprise.AuditSubscription;
import com.github.houbb.core.audit.application.domain.enterprise.WebhookDelivery;

import java.util.List;
import java.util.Optional;

/**
 * P9 Subscription 仓储端口
 * <p>管理 Webhook/Streaming 事件订阅。</p>
 */
public interface AuditSubscriptionRepository {

    AuditSubscription save(AuditSubscription subscription);

    Optional<AuditSubscription> findById(String id);

    List<AuditSubscription> findAllEnabled();

    List<AuditSubscription> findByEventType(String eventType);

    List<AuditSubscription> findByModule(String module);

    List<AuditSubscription> findAll();

    void updateLastSent(String id, String lastSentAt, String lastStatus, String errorMessage);

    void deleteById(String id);

    // ---- Webhook Delivery ----

    WebhookDelivery saveDelivery(WebhookDelivery delivery);

    List<WebhookDelivery> findDeliveriesBySubscriptionId(String subscriptionId, int limit);

    List<WebhookDelivery> findDeliveriesByAuditId(String auditId);

    long countDeliveriesToday();
}