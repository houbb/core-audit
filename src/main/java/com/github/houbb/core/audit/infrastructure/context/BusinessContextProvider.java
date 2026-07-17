package com.github.houbb.core.audit.infrastructure.context;

import com.github.houbb.core.audit.application.context.AuditContextProvider;
import com.github.houbb.core.audit.application.domain.context.AuditContext;
import com.github.houbb.core.audit.application.domain.context.BusinessContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * 业务上下文提供者
 * <p>自动填充 environment（从 Spring Profile），其他业务字段由 ContextResolver 从 metadata 桥接。</p>
 */
@Component
public class BusinessContextProvider implements AuditContextProvider {

    private final Environment environment;

    public BusinessContextProvider(Environment environment) {
        this.environment = environment;
    }

    @Override
    public int order() {
        return 400;
    }

    @Override
    public void contribute(AuditContext context) {
        BusinessContext bizCtx = new BusinessContext();

        // 从 Spring Profile 推导环境
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length > 0) {
            String profile = activeProfiles[0];
            if ("sqlite".equalsIgnoreCase(profile) || "dev".equalsIgnoreCase(profile)) {
                bizCtx.setEnvironment("development");
            } else if ("mysql".equalsIgnoreCase(profile) || "prod".equalsIgnoreCase(profile) || "production".equalsIgnoreCase(profile)) {
                bizCtx.setEnvironment("production");
            } else {
                bizCtx.setEnvironment(profile);
            }
        }

        context.setBusiness(bizCtx);
    }
}
