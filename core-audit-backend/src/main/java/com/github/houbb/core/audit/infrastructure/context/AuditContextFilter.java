package com.github.houbb.core.audit.infrastructure.context;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * 审计上下文 Servlet Filter
 * <p>确保 RequestContextHolder 已绑定请求，并为每个请求填充 MDC traceId。</p>
 * <p>注册为最高优先级，保证下游所有 Filter 和 Controller 都能获取请求上下文。</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class AuditContextFilter extends OncePerRequestFilter {

    private static final String TRACE_ID_MDC_KEY = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain chain) throws ServletException, IOException {
        try {
            // 确保 traceId 在 MDC 中可用
            String traceId = request.getHeader("X-Trace-Id");
            if (traceId == null || traceId.isBlank()) {
                traceId = request.getHeader("traceparent");
            }
            if (traceId == null || traceId.isBlank()) {
                traceId = UUID.randomUUID().toString();
            }
            MDC.put(TRACE_ID_MDC_KEY, traceId);

            chain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID_MDC_KEY);
        }
    }
}