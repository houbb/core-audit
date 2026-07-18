package com.github.houbb.core.audit.infrastructure.persistence.converter;

import com.github.houbb.core.audit.application.domain.enterprise.AuditSubscription;
import com.github.houbb.core.audit.application.domain.enterprise.WebhookDelivery;
import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditSubscriptionEntity;
import com.github.houbb.core.audit.infrastructure.persistence.entity.WebhookDeliveryEntity;

/**
 * P9 Subscription + WebhookDelivery 领域对象与实体转换器
 */
public class AuditSubscriptionConverter {

    private AuditSubscriptionConverter() {}

    public static AuditSubscription toDomain(AuditSubscriptionEntity entity) {
        if (entity == null) return null;
        return AuditSubscription.builder()
                .id(entity.getId())
                .subscriber(entity.getSubscriber())
                .eventType(entity.getEventType())
                .module(entity.getModule())
                .target(entity.getTarget())
                .targetUrl(entity.getTargetUrl())
                .secret(entity.getSecret())
                .retryCount(entity.getRetryCount())
                .timeoutMs(entity.getTimeoutMs())
                .enabled(entity.getEnabled())
                .lastSentAt(entity.getLastSentAt())
                .lastStatus(entity.getLastStatus())
                .errorMessage(entity.getErrorMessage())
                .build();
    }

    public static AuditSubscriptionEntity toEntity(AuditSubscription domain) {
        if (domain == null) return null;
        AuditSubscriptionEntity entity = new AuditSubscriptionEntity();
        entity.setId(domain.getId());
        entity.setSubscriber(domain.getSubscriber());
        entity.setEventType(domain.getEventType());
        entity.setModule(domain.getModule());
        entity.setTarget(domain.getTarget());
        entity.setTargetUrl(domain.getTargetUrl());
        entity.setSecret(domain.getSecret());
        entity.setRetryCount(domain.getRetryCount());
        entity.setTimeoutMs(domain.getTimeoutMs());
        entity.setEnabled(domain.getEnabled());
        entity.setLastSentAt(domain.getLastSentAt());
        entity.setLastStatus(domain.getLastStatus());
        entity.setErrorMessage(domain.getErrorMessage());
        return entity;
    }

    public static WebhookDelivery toDeliveryDomain(WebhookDeliveryEntity entity) {
        if (entity == null) return null;
        return WebhookDelivery.builder()
                .id(entity.getId())
                .subscriptionId(entity.getSubscriptionId())
                .auditId(entity.getAuditId())
                .requestUrl(entity.getRequestUrl())
                .requestBody(entity.getRequestBody())
                .responseCode(entity.getResponseCode())
                .responseBody(entity.getResponseBody())
                .durationMs(entity.getDurationMs())
                .status(entity.getStatus())
                .attempt(entity.getAttempt())
                .errorMessage(entity.getErrorMessage())
                .sentAt(entity.getSentAt())
                .build();
    }

    public static WebhookDeliveryEntity toDeliveryEntity(WebhookDelivery domain) {
        if (domain == null) return null;
        WebhookDeliveryEntity entity = new WebhookDeliveryEntity();
        entity.setId(domain.getId());
        entity.setSubscriptionId(domain.getSubscriptionId());
        entity.setAuditId(domain.getAuditId());
        entity.setRequestUrl(domain.getRequestUrl());
        entity.setRequestBody(domain.getRequestBody());
        entity.setResponseCode(domain.getResponseCode());
        entity.setResponseBody(domain.getResponseBody());
        entity.setDurationMs(domain.getDurationMs());
        entity.setStatus(domain.getStatus());
        entity.setAttempt(domain.getAttempt());
        entity.setErrorMessage(domain.getErrorMessage());
        entity.setSentAt(domain.getSentAt());
        return entity;
    }
}