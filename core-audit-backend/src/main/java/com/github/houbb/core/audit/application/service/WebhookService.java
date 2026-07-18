package com.github.houbb.core.audit.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.enterprise.AuditSubscription;
import com.github.houbb.core.audit.application.domain.enterprise.WebhookDelivery;
import com.github.houbb.core.audit.application.port.AuditSubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * P9 Webhook Runtime Service
 * <p>在 Audit Event 记录后，异步分发到已订阅的 Webhook URL。</p>
 *
 * <p>设计原则：</p>
 * <ul>
 *   <li>异步分发，不阻塞审计记录主流程</li>
 *   <li>失败重试（最多 retryCount 次）</li>
 *   <li>每次投递记录详细日志到 audit_webhook_delivery</li>
 *   <li>Fault isolation — 单个订阅失败不影响其他订阅</li>
 * </ul>
 */
@Service
public class WebhookService {

    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);

    private final AuditSubscriptionRepository subscriptionRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public WebhookService(AuditSubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    /**
     * 分发事件到所有匹配的订阅
     * <p>异步执行，在 AuditEventService.record() 后调用。</p>
     *
     * @param event 已保存的审计事件
     */
    @Async
    public void dispatch(AuditEvent event) {
        if (event == null) return;

        // Find matching subscriptions (by eventType AND by module)
        List<AuditSubscription> subscriptions = subscriptionRepository.findAllEnabled();

        for (AuditSubscription sub : subscriptions) {
            // Filter: event type must match (or wildcard)
            if (sub.getEventType() != null && !sub.getEventType().isEmpty()
                    && event.getEventType() != null
                    && !sub.getEventType().equals(event.getEventType().name())) {
                continue;
            }
            // Filter: module must match (or wildcard)
            if (sub.getModule() != null && !sub.getModule().isEmpty()
                    && event.getModule() != null
                    && !sub.getModule().equals(event.getModule().name())) {
                continue;
            }
            // Only dispatch to WEBHOOK targets
            if (!"WEBHOOK".equalsIgnoreCase(sub.getTarget())) {
                continue;
            }

            // Dispatch with retry
            dispatchWithRetry(sub, event);
        }
    }

    private void dispatchWithRetry(AuditSubscription sub, AuditEvent event) {
        int maxRetries = sub.getRetryCount() != null ? sub.getRetryCount() : 3;
        int timeoutMs = sub.getTimeoutMs() != null ? sub.getTimeoutMs() : 5000;
        String now = now();
        String requestBody = serializeEvent(event);

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            long startMs = System.currentTimeMillis();
            WebhookDelivery delivery = WebhookDelivery.builder()
                    .subscriptionId(sub.getId())
                    .auditId(event.getId())
                    .requestUrl(sub.getTargetUrl())
                    .requestBody(requestBody)
                    .attempt(attempt)
                    .sentAt(now)
                    .status("PENDING")
                    .build();

            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(sub.getTargetUrl()))
                        .timeout(Duration.ofMillis(timeoutMs))
                        .header("Content-Type", "application/json")
                        .header("X-Audit-Event-Id", event.getEventId())
                        .header("X-Audit-Signature", signBody(requestBody, sub.getSecret()))
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                delivery.setResponseCode(response.statusCode());
                delivery.setResponseBody(truncate(response.body(), 1000));
                delivery.setDurationMs((int) (System.currentTimeMillis() - startMs));

                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    delivery.setStatus("SUCCESS");
                    subscriptionRepository.saveDelivery(delivery);
                    subscriptionRepository.updateLastSent(sub.getId(), now, "SUCCESS", null);
                    log.debug("Webhook delivered: sub={}, audit={}, status={}, attempt={}",
                            sub.getSubscriber(), event.getId(), response.statusCode(), attempt);
                    return;
                } else {
                    delivery.setStatus("FAIL");
                    delivery.setErrorMessage("HTTP " + response.statusCode());
                    subscriptionRepository.saveDelivery(delivery);
                }
            } catch (Exception e) {
                delivery.setStatus(attempt < maxRetries ? "RETRYING" : "FAIL");
                delivery.setDurationMs((int) (System.currentTimeMillis() - startMs));
                delivery.setErrorMessage(e.getMessage());
                subscriptionRepository.saveDelivery(delivery);
                log.warn("Webhook failed: sub={}, audit={}, attempt={}/{}, error={}",
                        sub.getSubscriber(), event.getId(), attempt, maxRetries, e.getMessage());
            }
        }

        // All retries exhausted
        subscriptionRepository.updateLastSent(sub.getId(), now, "FAIL",
                "All " + maxRetries + " attempts failed");
    }

    private String serializeEvent(AuditEvent event) {
        try {
            Map<String, String> data = new java.util.LinkedHashMap<>();
            data.put("eventId", event.getEventId() != null ? event.getEventId() : "");
            data.put("module", event.getModule() != null ? event.getModule().name() : "");
            data.put("action", event.getAction() != null ? event.getAction().name() : "");
            data.put("eventType", event.getEventType() != null ? event.getEventType().name() : "");
            data.put("targetType", event.getTargetType() != null ? event.getTargetType() : "");
            data.put("targetId", event.getTargetId() != null ? event.getTargetId() : "");
            data.put("operatorId", event.getOperatorId() != null ? event.getOperatorId() : "");
            data.put("operatorName", event.getOperatorName() != null ? event.getOperatorName() : "");
            data.put("result", event.getResult() != null ? event.getResult().name() : "");
            data.put("description", event.getDescription() != null ? event.getDescription() : "");
            data.put("tenant", event.getTenant() != null ? event.getTenant() : "default");
            data.put("source", event.getSource() != null ? event.getSource() : "");
            data.put("createdAt", event.getCreatedAt() != null ? event.getCreatedAt().toString() : "");
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            return "{}";
        }
    }

    private String signBody(String body, String secret) {
        if (secret == null || secret.isEmpty()) return "";
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec spec = new javax.crypto.spec.SecretKeySpec(
                    secret.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(spec);
            byte[] hash = mac.doFinal(body.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            return "";
        }
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return null;
        return s.length() <= maxLen ? s : s.substring(0, maxLen) + "...";
    }

    private String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }

    // ======== Query Methods ========

    public List<AuditSubscription> getAllSubscriptions() {
        return subscriptionRepository.findAll();
    }

    public List<AuditSubscription> getEnabledSubscriptions() {
        return subscriptionRepository.findAllEnabled();
    }

    public AuditSubscription saveSubscription(AuditSubscription subscription) {
        return subscriptionRepository.save(subscription);
    }

    public void deleteSubscription(String id) {
        subscriptionRepository.deleteById(id);
    }

    public List<WebhookDelivery> getDeliveriesBySubscription(String subscriptionId, int limit) {
        return subscriptionRepository.findDeliveriesBySubscriptionId(subscriptionId, limit);
    }

    public List<WebhookDelivery> getDeliveriesByAuditId(String auditId) {
        return subscriptionRepository.findDeliveriesByAuditId(auditId);
    }

    public long countDeliveriesToday() {
        return subscriptionRepository.countDeliveriesToday();
    }
}