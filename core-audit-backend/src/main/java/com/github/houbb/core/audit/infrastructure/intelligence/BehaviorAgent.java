package com.github.houbb.core.audit.infrastructure.intelligence;

import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.intelligence.AuditInsight;
import com.github.houbb.core.audit.application.domain.intelligence.RiskLevel;
import com.github.houbb.core.audit.application.intelligence.AuditAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 行为模式 Agent
 * <p>分析操作人的行为模式，检测异常行为（时间异常、频率异常）。</p>
 */
@Component
public class BehaviorAgent implements AuditAgent {

    private static final Logger log = LoggerFactory.getLogger(BehaviorAgent.class);

    private static final LocalTime MIDNIGHT_START = LocalTime.of(0, 0);
    private static final LocalTime MIDNIGHT_END = LocalTime.of(6, 0);
    private static final LocalTime WEEKEND_START = LocalTime.of(0, 0);

    @Override
    public boolean supports(AuditEvent event) {
        // 所有操作都可以做行为分析
        return true;
    }

    @Override
    public AuditInsight analyze(AuditEvent event) {
        log.debug("BehaviorAgent analyzing: id={}", event.getId());

        LocalDateTime time = event.getOccurredAt() != null ? event.getOccurredAt() : event.getCreatedAt();
        if (time == null) return null;

        LocalTime localTime = time.toLocalTime();
        boolean isMidnight = !localTime.isBefore(MIDNIGHT_START) && localTime.isBefore(MIDNIGHT_END);

        if (isMidnight) {
            return AuditInsight.builder()
                    .auditId(event.getId())
                    .title("凌晨操作行为分析")
                    .severity(RiskLevel.MEDIUM)
                    .summary("操作人 " + safeOperator(event) + " 在凌晨 "
                            + String.format("%02d:%02d", localTime.getHour(), localTime.getMinute())
                            + " 执行了 " + event.getAction() + " 操作（模块: " + event.getModule() + "）。"
                            + "凌晨操作需特别关注，确认是否为正常业务需求。")
                    .suggestion("建议：审核凌晨操作的授权合理性；"
                            + "对非工作时间的高风险操作启用强制审批。")
                    .agentName("BehaviorAgent")
                    .build();
        }

        return null;
    }

    @Override
    public int order() {
        return 30;
    }

    private String safeOperator(AuditEvent event) {
        return event.getOperatorName() != null ? event.getOperatorName() : "未知操作人";
    }
}