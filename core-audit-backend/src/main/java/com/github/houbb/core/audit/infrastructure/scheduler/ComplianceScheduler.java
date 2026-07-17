package com.github.houbb.core.audit.infrastructure.scheduler;

import com.github.houbb.core.audit.application.domain.compliance.RetentionPolicy;
import com.github.houbb.core.audit.application.port.LegalHoldRepository;
import com.github.houbb.core.audit.application.port.RetentionPolicyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 合规定时任务
 * <p>包括保留策略执行和导出文件清理。</p>
 */
@Component
public class ComplianceScheduler {

    private static final Logger log = LoggerFactory.getLogger(ComplianceScheduler.class);

    private final RetentionPolicyRepository retentionPolicyRepository;
    private final LegalHoldRepository legalHoldRepository;
    private final JdbcTemplate jdbcTemplate;

    public ComplianceScheduler(RetentionPolicyRepository retentionPolicyRepository,
                               LegalHoldRepository legalHoldRepository,
                               JdbcTemplate jdbcTemplate) {
        this.retentionPolicyRepository = retentionPolicyRepository;
        this.legalHoldRepository = legalHoldRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 每日 02:00 执行保留策略
     * <p>遍历所有启用的保留策略，删除过期且不受法律保留的审计记录。</p>
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void executeRetention() {
        log.info("=== Starting retention job ===");
        int totalDeleted = 0;

        try {
            List<RetentionPolicy> policies = retentionPolicyRepository.findAllEnabled();
            log.info("Found {} enabled retention policies", policies.size());

            for (RetentionPolicy policy : policies) {
                try {
                    int deleted = processRetentionPolicy(policy);
                    totalDeleted += deleted;
                } catch (Exception e) {
                    log.error("Failed to process retention policy {}: {}", policy.getId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Retention job failed: {}", e.getMessage(), e);
        }

        log.info("=== Retention job completed: {} records deleted ===", totalDeleted);
    }

    /**
     * 处理单条保留策略
     */
    private int processRetentionPolicy(RetentionPolicy policy) {
        // 计算截止时间
        LocalDate cutoffDate = LocalDate.now().minusDays(policy.getRetentionDays());

        // 查询过期的审计事件 ID
        String sql = "SELECT ae.id FROM audit_event ae " +
                "WHERE ae.module = ? " +
                "AND ae.created_at < ? " +
                "ORDER BY ae.created_at ASC " +
                "LIMIT 1000";
        if (policy.getAction() != null) {
            sql = "SELECT ae.id FROM audit_event ae " +
                    "WHERE ae.module = ? " +
                    "AND ae.action = ? " +
                    "AND ae.created_at < ? " +
                    "ORDER BY ae.created_at ASC " +
                    "LIMIT 1000";
        }

        List<String> expiredIds;
        if (policy.getAction() != null) {
            expiredIds = jdbcTemplate.queryForList(sql, String.class,
                    policy.getModule().name(), policy.getAction().name(), cutoffDate.toString());
        } else {
            expiredIds = jdbcTemplate.queryForList(sql, String.class,
                    policy.getModule().name(), cutoffDate.toString());
        }

        int deleted = 0;
        for (String auditId : expiredIds) {
            try {
                // 检查法律保留
                if (legalHoldRepository.existsActiveByAuditId(auditId)) {
                    log.debug("Audit {} skipped: under legal hold", auditId);
                    continue;
                }

                // 级联删除（签名 → 法律保留 → 变更 → 快照 → 事件）
                jdbcTemplate.update("DELETE FROM audit_signature WHERE audit_id = ?", auditId);
                jdbcTemplate.update("DELETE FROM audit_legal_hold WHERE audit_id = ?", auditId);
                jdbcTemplate.update("DELETE FROM audit_change WHERE audit_id = ?", auditId);
                jdbcTemplate.update("DELETE FROM audit_snapshot WHERE audit_id = ?", auditId);
                jdbcTemplate.update("DELETE FROM audit_event WHERE id = ?", auditId);

                deleted++;
            } catch (Exception e) {
                log.warn("Failed to delete expired audit {}: {}", auditId, e.getMessage());
            }
        }

        if (deleted > 0) {
            log.info("Retention policy {} ({}/{}) → {} records deleted (cutoff: {})",
                    policy.getId(), policy.getModule(), policy.getAction(),
                    deleted, cutoffDate);
        }

        return deleted;
    }
}