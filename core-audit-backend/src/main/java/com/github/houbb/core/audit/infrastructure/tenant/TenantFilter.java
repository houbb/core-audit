package com.github.houbb.core.audit.infrastructure.tenant;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * P9 Tenant Filter — 从 HTTP Header 提取租户 ID
 * <p>在每个请求中从 X-Tenant-Id header 提取租户信息，
 * 存入 ThreadLocal 供后续 AuditEventService 自动填充。</p>
 *
 * <p>order=0 = 最高优先级，在所有 Filter 之前执行。</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TenantFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(TenantFilter.class);

    private static final String TENANT_HEADER = "X-Tenant-Id";
    private static final String DEFAULT_TENANT = "default";

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String tenant = DEFAULT_TENANT;
        if (request instanceof HttpServletRequest httpRequest) {
            String headerValue = httpRequest.getHeader(TENANT_HEADER);
            if (headerValue != null && !headerValue.isBlank()) {
                tenant = headerValue.trim();
            }
        }
        CURRENT_TENANT.set(tenant);
        try {
            chain.doFilter(request, response);
        } finally {
            CURRENT_TENANT.remove();
        }
    }

    /**
     * 获取当前请求的租户 ID
     */
    public static String getCurrentTenant() {
        String tenant = CURRENT_TENANT.get();
        return tenant != null ? tenant : DEFAULT_TENANT;
    }
}