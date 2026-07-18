package com.github.houbb.core.audit.infrastructure.persistence.repository;

import com.github.houbb.core.audit.application.domain.enterprise.AuditSubscription;
import com.github.houbb.core.audit.application.domain.enterprise.WebhookDelivery;
import com.github.houbb.core.audit.application.port.AuditSubscriptionRepository;
import com.github.houbb.core.audit.infrastructure.persistence.converter.AuditSubscriptionConverter;
import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditSubscriptionEntity;
import com.github.houbb.core.audit.infrastructure.persistence.entity.WebhookDeliveryEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * P9 JDBC 实现 AuditSubscriptionRepository
 */
@Repository
public class JdbcAuditSubscriptionRepository implements AuditSubscriptionRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SubscriptionRowMapper subRowMapper = new SubscriptionRowMapper();
    private final DeliveryRowMapper deliveryRowMapper = new DeliveryRowMapper();

    public JdbcAuditSubscriptionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public AuditSubscription save(AuditSubscription sub) {
        AuditSubscriptionEntity entity = AuditSubscriptionConverter.toEntity(sub);
        String now = now();

        if (entity.getId() == null || entity.getId().isBlank()) {
            entity.setId(UUID.randomUUID().toString());
        }

        jdbcTemplate.update("DELETE FROM audit_subscription WHERE id = ?", entity.getId());

        jdbcTemplate.update(
                "INSERT INTO audit_subscription (id, subscriber, event_type, module, target, target_url, " +
                "secret, retry_count, timeout_ms, enabled, last_sent_at, last_status, error_message, " +
                "create_time, update_time, create_user, update_user) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                entity.getId(), entity.getSubscriber(), entity.getEventType(), entity.getModule(),
                entity.getTarget(), entity.getTargetUrl(),
                entity.getSecret(), entity.getRetryCount() != null ? entity.getRetryCount() : 3,
                entity.getTimeoutMs() != null ? entity.getTimeoutMs() : 5000,
                entity.getEnabled() != null ? entity.getEnabled() : 1,
                entity.getLastSentAt(), entity.getLastStatus(), entity.getErrorMessage(),
                entity.getCreateTime() != null ? entity.getCreateTime() : now,
                now, "system", "system"
        );

        return findById(entity.getId()).orElse(sub);
    }

    @Override
    public Optional<AuditSubscription> findById(String id) {
        List<AuditSubscriptionEntity> results = jdbcTemplate.query(
                "SELECT * FROM audit_subscription WHERE id = ?", subRowMapper, id);
        return results.stream().findFirst().map(AuditSubscriptionConverter::toDomain);
    }

    @Override
    public List<AuditSubscription> findAllEnabled() {
        return jdbcTemplate.query(
                "SELECT * FROM audit_subscription WHERE enabled = 1 ORDER BY subscriber",
                subRowMapper).stream().map(AuditSubscriptionConverter::toDomain).toList();
    }

    @Override
    public List<AuditSubscription> findByEventType(String eventType) {
        return jdbcTemplate.query(
                "SELECT * FROM audit_subscription WHERE enabled = 1 AND (event_type = ? OR event_type IS NULL) ORDER BY subscriber",
                subRowMapper, eventType).stream().map(AuditSubscriptionConverter::toDomain).toList();
    }

    @Override
    public List<AuditSubscription> findByModule(String module) {
        return jdbcTemplate.query(
                "SELECT * FROM audit_subscription WHERE enabled = 1 AND (module = ? OR module IS NULL) ORDER BY subscriber",
                subRowMapper, module).stream().map(AuditSubscriptionConverter::toDomain).toList();
    }

    @Override
    public List<AuditSubscription> findAll() {
        return jdbcTemplate.query(
                "SELECT * FROM audit_subscription ORDER BY subscriber",
                subRowMapper).stream().map(AuditSubscriptionConverter::toDomain).toList();
    }

    @Override
    public void updateLastSent(String id, String lastSentAt, String lastStatus, String errorMessage) {
        jdbcTemplate.update(
                "UPDATE audit_subscription SET last_sent_at = ?, last_status = ?, error_message = ?, update_time = ? WHERE id = ?",
                lastSentAt, lastStatus, errorMessage, now(), id);
    }

    @Override
    public void deleteById(String id) {
        jdbcTemplate.update("DELETE FROM audit_subscription WHERE id = ?", id);
    }

    // ======== Webhook Delivery ========

    @Override
    public WebhookDelivery saveDelivery(WebhookDelivery delivery) {
        WebhookDeliveryEntity entity = AuditSubscriptionConverter.toDeliveryEntity(delivery);
        String now = now();

        if (entity.getId() == null || entity.getId().isBlank()) {
            entity.setId(UUID.randomUUID().toString());
        }

        jdbcTemplate.update(
                "INSERT INTO audit_webhook_delivery (id, subscription_id, audit_id, request_url, request_body, " +
                "response_code, response_body, duration_ms, status, attempt, error_message, sent_at, " +
                "create_time, update_time, create_user, update_user) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                entity.getId(), entity.getSubscriptionId(), entity.getAuditId(),
                entity.getRequestUrl(), entity.getRequestBody(),
                entity.getResponseCode(), entity.getResponseBody(), entity.getDurationMs(),
                entity.getStatus(), entity.getAttempt() != null ? entity.getAttempt() : 1,
                entity.getErrorMessage(), entity.getSentAt(),
                entity.getCreateTime() != null ? entity.getCreateTime() : now,
                now, "system", "system"
        );

        return AuditSubscriptionConverter.toDeliveryDomain(entity);
    }

    @Override
    public List<WebhookDelivery> findDeliveriesBySubscriptionId(String subscriptionId, int limit) {
        return jdbcTemplate.query(
                "SELECT * FROM audit_webhook_delivery WHERE subscription_id = ? ORDER BY sent_at DESC LIMIT ?",
                deliveryRowMapper, subscriptionId, limit)
                .stream().map(AuditSubscriptionConverter::toDeliveryDomain).toList();
    }

    @Override
    public List<WebhookDelivery> findDeliveriesByAuditId(String auditId) {
        return jdbcTemplate.query(
                "SELECT * FROM audit_webhook_delivery WHERE audit_id = ? ORDER BY sent_at DESC",
                deliveryRowMapper, auditId)
                .stream().map(AuditSubscriptionConverter::toDeliveryDomain).toList();
    }

    @Override
    public long countDeliveriesToday() {
        String today = LocalDate.now().toString();
        Long result = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM audit_webhook_delivery WHERE sent_at >= ?", Long.class, today);
        return result != null ? result : 0;
    }

    private String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }

    private static class SubscriptionRowMapper implements RowMapper<AuditSubscriptionEntity> {
        @Override
        public AuditSubscriptionEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
            AuditSubscriptionEntity e = new AuditSubscriptionEntity();
            e.setId(rs.getString("id"));
            e.setSubscriber(rs.getString("subscriber"));
            e.setEventType(rs.getString("event_type"));
            e.setModule(rs.getString("module"));
            e.setTarget(rs.getString("target"));
            e.setTargetUrl(rs.getString("target_url"));
            e.setSecret(rs.getString("secret"));
            e.setRetryCount(rs.getInt("retry_count"));
            e.setTimeoutMs(rs.getInt("timeout_ms"));
            e.setEnabled(rs.getInt("enabled"));
            e.setLastSentAt(rs.getString("last_sent_at"));
            e.setLastStatus(rs.getString("last_status"));
            e.setErrorMessage(rs.getString("error_message"));
            e.setCreateTime(rs.getString("create_time"));
            e.setUpdateTime(rs.getString("update_time"));
            e.setCreateUser(rs.getString("create_user"));
            e.setUpdateUser(rs.getString("update_user"));
            return e;
        }
    }

    private static class DeliveryRowMapper implements RowMapper<WebhookDeliveryEntity> {
        @Override
        public WebhookDeliveryEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
            WebhookDeliveryEntity e = new WebhookDeliveryEntity();
            e.setId(rs.getString("id"));
            e.setSubscriptionId(rs.getString("subscription_id"));
            e.setAuditId(rs.getString("audit_id"));
            e.setRequestUrl(rs.getString("request_url"));
            e.setRequestBody(rs.getString("request_body"));
            e.setResponseCode(rs.getObject("response_code") != null ? rs.getInt("response_code") : null);
            e.setResponseBody(rs.getString("response_body"));
            e.setDurationMs(rs.getObject("duration_ms") != null ? rs.getInt("duration_ms") : null);
            e.setStatus(rs.getString("status"));
            e.setAttempt(rs.getObject("attempt") != null ? rs.getInt("attempt") : 1);
            e.setErrorMessage(rs.getString("error_message"));
            e.setSentAt(rs.getString("sent_at"));
            e.setCreateTime(rs.getString("create_time"));
            e.setUpdateTime(rs.getString("update_time"));
            e.setCreateUser(rs.getString("create_user"));
            e.setUpdateUser(rs.getString("update_user"));
            return e;
        }
    }
}