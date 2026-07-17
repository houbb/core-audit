package com.github.houbb.core.audit.application.service;

import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.enums.AuditAction;
import com.github.houbb.core.audit.application.domain.enums.AuditModule;
import com.github.houbb.core.audit.application.domain.intelligence.RiskLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 内置规则引擎
 * <p>Rule First — 确定性规则命中，稳定可靠。后续可通过 SPI 扩展自定义规则。</p>
 *
 * <p>核心规则：</p>
 * <ol>
 *   <li>凌晨 (00:00-06:00) + DELETE → HIGH</li>
 *   <li>批量操作 (>100) + DELETE → CRITICAL</li>
 *   <li>Root/Admin + 非工作时间登录 → MEDIUM</li>
 *   <li>首次操作 + 敏感模块 (CONFIG/AI) → MEDIUM</li>
 *   <li>短时间内高频操作 (>50次/分钟) → HIGH</li>
 * </ol>
 */
@Service
public class RuleEngine {

    private static final Logger log = LoggerFactory.getLogger(RuleEngine.class);

    /**
     * 凌晨时段：00:00 - 06:00
     */
    private static final LocalTime MIDNIGHT_START = LocalTime.of(0, 0);
    private static final LocalTime MIDNIGHT_END = LocalTime.of(6, 0);

    /**
     * 工作时间：09:00 - 18:00
     */
    private static final LocalTime WORK_START = LocalTime.of(9, 0);
    private static final LocalTime WORK_END = LocalTime.of(18, 0);

    /**
     * 评估审计事件的风险
     *
     * @param event 审计事件
     * @return 规则评估结果（null = 无规则命中）
     */
    public RuleResult evaluate(AuditEvent event) {
        if (event == null) return null;

        // Rule 1: 凌晨 + DELETE → HIGH
        RuleResult r1 = evaluateMidnightDelete(event);
        if (r1 != null) return r1;

        // Rule 2: 批量 DELETE + 涉及大量目标 → CRITICAL
        RuleResult r2 = evaluateBulkDelete(event);
        if (r2 != null) return r2;

        // Rule 3: Admin + 非工作时间登录 → MEDIUM
        RuleResult r3 = evaluateOffHoursAdminLogin(event);
        if (r3 != null) return r3;

        // Rule 4: 敏感模块首次操作 → MEDIUM
        RuleResult r4 = evaluateSensitiveModuleFirstAccess(event);
        if (r4 != null) return r4;

        // Rule 5: 高频操作 → HIGH (marker only, actual detection needs pattern engine)
        // This rule is evaluated by PatternEngine, not here directly

        return null;
    }

    /**
     * Rule 1: 凌晨 DELETE → HIGH
     * <p>凌晨时段（00:00-06:00）的删除操作视为高风险</p>
     */
    private RuleResult evaluateMidnightDelete(AuditEvent event) {
        if (event.getAction() != AuditAction.DELETE) return null;

        LocalDateTime time = event.getOccurredAt() != null ? event.getOccurredAt() : event.getCreatedAt();
        if (time == null) return null;

        LocalTime localTime = time.toLocalTime();
        // Midnight: 00:00 (inclusive) to 06:00 (exclusive)
        boolean isMidnight = (localTime.equals(MIDNIGHT_START) || localTime.isAfter(MIDNIGHT_START))
                && localTime.isBefore(MIDNIGHT_END);
        if (isMidnight) {
            return new RuleResult(70, RiskLevel.HIGH,
                    "凌晨 " + String.format("%02d:%02d", localTime.getHour(), localTime.getMinute()) +
                    " 执行了 DELETE 操作（模块: " + event.getModule() + "）",
                    "midnight-delete");
        }
        return null;
    }

    /**
     * Rule 2: 批量删除 → CRITICAL
     * <p>当 metadata 中包含 count 或 batchSize 且 > 100 时，视为严重批量删除</p>
     */
    private RuleResult evaluateBulkDelete(AuditEvent event) {
        if (event.getAction() != AuditAction.DELETE) return null;
        if (event.getMetadata() == null) return null;

        Object countObj = event.getMetadata().getOrDefault("count",
                event.getMetadata().get("batchSize"));
        if (countObj == null) return null;

        int count;
        try {
            count = Integer.parseInt(countObj.toString());
        } catch (NumberFormatException e) {
            return null;
        }

        if (count > 100) {
            return new RuleResult(95, RiskLevel.CRITICAL,
                    "批量删除了 " + count + " 条记录（模块: " + event.getModule() + "）",
                    "bulk-delete");
        }
        if (count > 10) {
            return new RuleResult(60, RiskLevel.HIGH,
                    "批量删除了 " + count + " 条记录（模块: " + event.getModule() + "）",
                    "bulk-delete-medium");
        }
        return null;
    }

    /**
     * Rule 3: Root/Admin + 非工作时间登录 → MEDIUM
     */
    private RuleResult evaluateOffHoursAdminLogin(AuditEvent event) {
        if (event.getAction() != AuditAction.LOGIN) return null;

        String operatorName = event.getOperatorName();
        if (operatorName == null) return null;
        String lower = operatorName.toLowerCase();
        if (!lower.contains("root") && !lower.contains("admin")) return null;

        LocalDateTime time = event.getOccurredAt() != null ? event.getOccurredAt() : event.getCreatedAt();
        if (time == null) return null;

        LocalTime localTime = time.toLocalTime();
        if (localTime.isBefore(WORK_START) || localTime.isAfter(WORK_END)) {
            return new RuleResult(40, RiskLevel.MEDIUM,
                    "管理员 " + operatorName + " 在非工作时间登录（" +
                    String.format("%02d:%02d", localTime.getHour(), localTime.getMinute()) + "）",
                    "off-hours-admin-login");
        }
        return null;
    }

    /**
     * Rule 4: 敏感模块首次操作 → MEDIUM
     * <p>对 CONFIG、AI 模块的首次操作标记为需关注</p>
     */
    private RuleResult evaluateSensitiveModuleFirstAccess(AuditEvent event) {
        AuditModule module = event.getModule();
        if (module != AuditModule.CONFIG && module != AuditModule.AI) return null;

        // Check if this appears to be a first-time access (metadata flag or context hint)
        if (event.getMetadata() != null) {
            Object isFirst = event.getMetadata().get("_firstAccess");
            if ("true".equals(String.valueOf(isFirst))) {
                return new RuleResult(35, RiskLevel.MEDIUM,
                        "敏感模块 " + module + " 的首次操作（操作: " + event.getAction() + "）",
                        "sensitive-module-first-access");
            }
        }
        return null;
    }

    /**
     * 规则评估结果
     */
    public static class RuleResult {
        private final int score;
        private final RiskLevel level;
        private final String reason;
        private final String ruleName;

        public RuleResult(int score, RiskLevel level, String reason, String ruleName) {
            this.score = score;
            this.level = level;
            this.reason = reason;
            this.ruleName = ruleName;
        }

        public int getScore() { return score; }
        public RiskLevel getLevel() { return level; }
        public String getReason() { return reason; }
        public String getRuleName() { return ruleName; }
    }
}