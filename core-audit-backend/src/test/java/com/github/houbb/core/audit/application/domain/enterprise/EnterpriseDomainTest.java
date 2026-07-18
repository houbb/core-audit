package com.github.houbb.core.audit.application.domain.enterprise;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * P9 Enterprise Domain Model Tests
 */
class EnterpriseDomainTest {

    @Test
    @DisplayName("P9: AuditSource builder creates valid domain")
    void shouldCreateAuditSource() {
        AuditSource source = AuditSource.builder()
                .name("core-user")
                .type("INTERNAL")
                .version("2.0")
                .tenant("default")
                .status("ACTIVE")
                .description("User management service")
                .build();

        assertEquals("core-user", source.getName());
        assertEquals("INTERNAL", source.getType());
        assertEquals("ACTIVE", source.getStatus());
        assertEquals("default", source.getTenant());
    }

    @Test
    @DisplayName("P9: AuditProvider builder creates valid domain")
    void shouldCreateAuditProvider() {
        AuditProvider provider = AuditProvider.builder()
                .plugin("sap-audit")
                .providerClass("com.example.SapAuditProvider")
                .providerType("PROVIDER")
                .version("1.0")
                .tenant("default")
                .status("ACTIVE")
                .build();

        assertEquals("sap-audit", provider.getPlugin());
        assertEquals("PROVIDER", provider.getProviderType());
    }

    @Test
    @DisplayName("P9: AuditSubscription builder creates valid domain")
    void shouldCreateAuditSubscription() {
        AuditSubscription sub = AuditSubscription.builder()
                .subscriber("Slack")
                .eventType("USER_CREATED")
                .target("WEBHOOK")
                .targetUrl("https://hooks.slack.com/services/xxx")
                .enabled(1)
                .retryCount(3)
                .timeoutMs(5000)
                .build();

        assertEquals("Slack", sub.getSubscriber());
        assertEquals("WEBHOOK", sub.getTarget());
        assertEquals(1, sub.getEnabled());
    }

    @Test
    @DisplayName("P9: WebhookDelivery builder creates valid domain")
    void shouldCreateWebhookDelivery() {
        WebhookDelivery delivery = WebhookDelivery.builder()
                .subscriptionId("sub-1")
                .auditId("audit-1")
                .requestUrl("https://hooks.slack.com/xxx")
                .status("SUCCESS")
                .responseCode(200)
                .durationMs(150)
                .attempt(1)
                .build();

        assertEquals("sub-1", delivery.getSubscriptionId());
        assertEquals("SUCCESS", delivery.getStatus());
        assertEquals(200, delivery.getResponseCode());
    }

    @Test
    @DisplayName("P9: Domain objects have correct setters")
    void shouldSupportSetters() {
        AuditSource source = new AuditSource();
        source.setId("src-1");
        source.setName("test");
        source.setType("EXTERNAL");
        source.setStatus("ACTIVE");

        assertEquals("src-1", source.getId());
        assertEquals("test", source.getName());
        assertEquals("EXTERNAL", source.getType());
        assertEquals("ACTIVE", source.getStatus());
    }
}