package com.github.houbb.core.audit.api.controller;

import com.github.houbb.core.audit.application.domain.enterprise.AuditSubscription;
import com.github.houbb.core.audit.application.domain.enterprise.WebhookDelivery;
import com.github.houbb.core.audit.application.service.WebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * P9 Webhook Subscription API — Webhook 订阅管理
 */
@RestController
@RequestMapping("/api/v1/audit/enterprise")
@Tag(name = "Enterprise — Webhook", description = "P9 Webhook 订阅管理 API")
public class WebhookController {

    private final WebhookService webhookService;

    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping("/subscriptions")
    @Operation(summary = "创建/更新订阅")
    public ResponseEntity<AuditSubscription> save(@RequestBody AuditSubscription subscription) {
        return ResponseEntity.ok(webhookService.saveSubscription(subscription));
    }

    @GetMapping("/subscriptions")
    @Operation(summary = "获取所有订阅")
    public ResponseEntity<List<AuditSubscription>> listAll() {
        return ResponseEntity.ok(webhookService.getAllSubscriptions());
    }

    @GetMapping("/subscriptions/enabled")
    @Operation(summary = "获取已启用订阅")
    public ResponseEntity<List<AuditSubscription>> listEnabled() {
        return ResponseEntity.ok(webhookService.getEnabledSubscriptions());
    }

    @DeleteMapping("/subscriptions/{id}")
    @Operation(summary = "删除订阅")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        webhookService.deleteSubscription(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/subscriptions/{id}/deliveries")
    @Operation(summary = "获取订阅投递日志")
    public ResponseEntity<List<WebhookDelivery>> deliveries(
            @PathVariable String id,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(webhookService.getDeliveriesBySubscription(id, limit));
    }

    @GetMapping("/webhook-deliveries/{auditId}")
    @Operation(summary = "按审计事件获取投递日志")
    public ResponseEntity<List<WebhookDelivery>> deliveriesByAudit(@PathVariable String auditId) {
        return ResponseEntity.ok(webhookService.getDeliveriesByAuditId(auditId));
    }
}