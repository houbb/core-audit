package com.github.houbb.core.audit.infrastructure.timeline;

import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.timeline.TimelineKey;
import com.github.houbb.core.audit.application.timeline.TimelineStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 工作流时间线策略
 * <p>依据 Workflow ID 将同一工作流内的所有事件聚合为一条 WORKFLOW Timeline。</p>
 * <p>Workflow ID 来源：HTTP Header X-Workflow-Id → RequestContext.workflowId → BusinessContext.workflowId。</p>
 */
@Component
public class WorkflowTimelineStrategy implements TimelineStrategy {

    private static final Logger log = LoggerFactory.getLogger(WorkflowTimelineStrategy.class);

    @Override
    public boolean supports(AuditEvent event) {
        if (event.getContext() == null) {
            return false;
        }
        // 优先从 BusinessContext 取（metadata 桥接或业务代码显式设置）
        if (event.getContext().getBusiness() != null) {
            String workflowId = event.getContext().getBusiness().getWorkflowId();
            if (workflowId != null && !workflowId.isBlank()) {
                return true;
            }
        }
        // 其次从 RequestContext 取（HTTP header 自动采集）
        if (event.getContext().getRequest() != null) {
            String workflowId = event.getContext().getRequest().getWorkflowId();
            return workflowId != null && !workflowId.isBlank();
        }
        return false;
    }

    @Override
    public TimelineKey resolve(AuditEvent event) {
        String workflowId = null;
        if (event.getContext().getBusiness() != null) {
            workflowId = event.getContext().getBusiness().getWorkflowId();
        }
        if (workflowId == null && event.getContext().getRequest() != null) {
            workflowId = event.getContext().getRequest().getWorkflowId();
        }
        return TimelineKey.of("WORKFLOW", workflowId);
    }

    @Override
    public int order() {
        return 500;
    }
}
