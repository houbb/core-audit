package com.github.houbb.core.audit.infrastructure.context;

import com.github.houbb.core.audit.application.context.AuditContextProvider;
import com.github.houbb.core.audit.application.domain.context.AuditContext;
import com.github.houbb.core.audit.application.domain.context.SystemContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 系统上下文提供者
 * <p>从 Spring Environment 和系统属性自动填充服务、版本、主机名、线程等信息。</p>
 */
@Component
public class SystemContextProvider implements AuditContextProvider {

    private static final Logger log = LoggerFactory.getLogger(SystemContextProvider.class);

    private final Environment environment;

    public SystemContextProvider(Environment environment) {
        this.environment = environment;
    }

    @Override
    public int order() {
        return 500;
    }

    @Override
    public void contribute(AuditContext context) {
        SystemContext sysCtx = new SystemContext();

        // Service name
        sysCtx.setService(environment.getProperty("spring.application.name"));

        // Version
        sysCtx.setVersion(environment.getProperty("info.app.version"));

        // Hostname
        try {
            sysCtx.setHostname(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            log.debug("Failed to resolve hostname: {}", e.getMessage());
        }

        // Thread
        sysCtx.setThread(Thread.currentThread().getName());

        // Instance / Node / Cluster — from env vars or properties
        sysCtx.setInstance(environment.getProperty("INSTANCE_ID"));
        sysCtx.setNode(environment.getProperty("NODE_NAME"));
        sysCtx.setCluster(environment.getProperty("CLUSTER_NAME"));

        context.setSystem(sysCtx);
    }
}
