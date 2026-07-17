package com.github.houbb.core.audit.application.context;

import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.context.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 上下文解析器 — 编排所有 AuditContextProvider
 * <p>创建空 AuditContext，按 order() 顺序调用所有 Provider，最后将完成的 context 挂载到 AuditEvent 上。</p>
 * <p>核心原则：自动采集优先 — 业务无需手动填写。</p>
 * <p>Provider 故障隔离：单个 Provider 失败不影响其他 Provider 和审计记录的写入。</p>
 */
@Component
public class ContextResolver {

    private static final Logger log = LoggerFactory.getLogger(ContextResolver.class);

    /** 从 AuditEvent.metadata 桥接到 BusinessContext 的已知 key */
    private static final List<String> BUSINESS_METADATA_KEYS = List.of(
            "workspace", "project", "businessId", "resource", "resourceId", "sessionId", "workflowId"
    );

    private final List<AuditContextProvider> providers;

    /**
     * 构造函数 — Spring 自动注入所有 AuditContextProvider Bean
     * <p>无 Provider 时 List 为空，Resolver 仍正常工作（context 保持 empty 状态）。</p>
     */
    public ContextResolver(List<AuditContextProvider> providers) {
        this.providers = providers.stream()
                .sorted(Comparator.comparingInt(AuditContextProvider::order))
                .toList();
        log.info("ContextResolver initialized with {} provider(s)", this.providers.size());
    }

    /**
     * 解析并挂载完整上下文到事件
     * <p>由 AuditEventService.record() 自动调用，对业务完全透明。</p>
     *
     * @param event 审计事件
     */
    public void resolve(AuditEvent event) {
        AuditContext context = AuditContext.empty();

        for (AuditContextProvider provider : providers) {
            try {
                provider.contribute(context);
            } catch (Exception e) {
                log.warn("Provider '{}' failed to contribute context: {}",
                        provider.getClass().getSimpleName(), e.getMessage());
                // 故障隔离 — 一个 Provider 失败不阻塞其他 Provider
            }
        }

        // 从 metadata 桥接业务上下文字段
        mergeFromMetadata(context, event.getMetadata());

        event.setContext(context);
    }

    /**
     * 如果 BusinessContext 字段仍为空，从 AuditEvent.metadata 中桥接已知 key
     */
    private void mergeFromMetadata(AuditContext context, Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return;
        }

        BusinessContext biz = context.getBusiness();
        if (biz == null) {
            biz = new BusinessContext();
            context.setBusiness(biz);
        }

        for (String key : BUSINESS_METADATA_KEYS) {
            Object value = metadata.get(key);
            if (value == null) {
                continue;
            }
            switch (key) {
                case "workspace":
                    if (isEmpty(biz.getWorkspace())) biz.setWorkspace(value.toString());
                    break;
                case "project":
                    if (isEmpty(biz.getProject())) biz.setProject(value.toString());
                    break;
                case "businessId":
                    if (isEmpty(biz.getBusinessId())) biz.setBusinessId(value.toString());
                    break;
                case "resource":
                    if (isEmpty(biz.getResource())) biz.setResource(value.toString());
                    break;
                case "resourceId":
                    if (isEmpty(biz.getResourceId())) biz.setResourceId(value.toString());
                    break;
                case "sessionId":
                    if (isEmpty(biz.getSessionId())) biz.setSessionId(value.toString());
                    break;
                case "workflowId":
                    if (isEmpty(biz.getWorkflowId())) biz.setWorkflowId(value.toString());
                    break;
            }
        }
    }

    private boolean isEmpty(String s) {
        return s == null || s.isBlank();
    }
}