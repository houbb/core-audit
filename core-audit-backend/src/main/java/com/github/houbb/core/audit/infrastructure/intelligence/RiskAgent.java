package com.github.houbb.core.audit.infrastructure.intelligence;

import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.enums.AuditAction;
import com.github.houbb.core.audit.application.domain.intelligence.AuditInsight;
import com.github.houbb.core.audit.application.domain.intelligence.RiskLevel;
import com.github.houbb.core.audit.application.intelligence.AuditAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 风险评估 Agent
 * <p>专门评估 DELETE、LOGIN、权限变更等高风险操作的安全风险。</p>
 */
@Component
public class RiskAgent implements AuditAgent {

    private static final Logger log = LoggerFactory.getLogger(RiskAgent.class);

    @Override
    public boolean supports(AuditEvent event) {
        if (event == null) return false;
        return event.getAction() == AuditAction.DELETE
                || event.getAction() == AuditAction.LOGIN
                || event.getAction() == AuditAction.LOGOUT
                || event.getAction() == AuditAction.ENABLE
                || event.getAction() == AuditAction.DISABLE;
    }

    @Override
    public AuditInsight analyze(AuditEvent event) {
        log.debug("RiskAgent analyzing: id={}, action={}", event.getId(), event.getAction());

        return switch (event.getAction()) {
            case DELETE ->
                    AuditInsight.builder()
                            .auditId(event.getId())
                            .title("删除操作风险评估")
                            .severity(RiskLevel.HIGH)
                            .summary("操作人 " + safeOperator(event) + " 执行了 DELETE 操作，"
                                    + "目标: " + event.getTargetType() + "/" + event.getTargetId()
                                    + "。删除操作不可逆，请确认数据的合法性和备份状态。")
                            .suggestion("建议：启用审批流程；对删除操作增加二次确认弹窗；"
                                    + "对批量删除操作限制单次最大数量。")
                            .agentName("RiskAgent")
                            .build();
            case LOGIN ->
                    AuditInsight.builder()
                            .auditId(event.getId())
                            .title("登录操作记录")
                            .severity(RiskLevel.LOW)
                            .summary("操作人 " + safeOperator(event) + " 登录系统。")
                            .agentName("RiskAgent")
                            .build();
            case ENABLE, DISABLE ->
                    AuditInsight.builder()
                            .auditId(event.getId())
                            .title("权限变更检测")
                            .severity(RiskLevel.MEDIUM)
                            .summary("操作人 " + safeOperator(event) + " 执行了 "
                                    + event.getAction() + " 操作，"
                                    + "目标: " + event.getTargetType() + "/" + event.getTargetId()
                                    + "。权限变更可能影响系统安全。")
                            .suggestion("建议：审查权限变更的合理性；定期审计权限分配。")
                            .agentName("RiskAgent")
                            .build();
            default -> null;
        };
    }

    @Override
    public int order() {
        return 10;
    }

    private String safeOperator(AuditEvent event) {
        return event.getOperatorName() != null ? event.getOperatorName() : "未知操作人";
    }
}