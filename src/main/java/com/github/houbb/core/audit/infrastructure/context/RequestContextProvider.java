package com.github.houbb.core.audit.infrastructure.context;

import com.github.houbb.core.audit.application.context.AuditContextProvider;
import com.github.houbb.core.audit.application.domain.context.AuditContext;
import com.github.houbb.core.audit.application.domain.context.RequestContext;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * HTTP 请求上下文提供者
 * <p>从当前线程绑定的 HTTP 请求自动提取请求上下文信息。</p>
 * <p>非 HTTP 线程（如异步任务）中 RequestContextHolder 为空，优雅跳过。</p>
 */
@Component
public class RequestContextProvider implements AuditContextProvider {

    private static final Logger log = LoggerFactory.getLogger(RequestContextProvider.class);

    @Override
    public int order() {
        return 100;
    }

    @Override
    public void contribute(AuditContext context) {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes)
                    RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attrs.getRequest();

            RequestContext reqCtx = new RequestContext();
            reqCtx.setRequestUri(request.getRequestURI());
            reqCtx.setMethod(request.getMethod());
            reqCtx.setReferer(request.getHeader("Referer"));
            reqCtx.setUserAgent(request.getHeader("User-Agent"));
            reqCtx.setHost(request.getHeader("Host"));
            reqCtx.setProtocol(request.getProtocol());

            // Trace ID: 优先从 header 获取，其次 MDC，最后生成
            String traceId = request.getHeader("X-Trace-Id");
            if (isEmpty(traceId)) {
                traceId = request.getHeader("traceparent");
            }
            if (isEmpty(traceId)) {
                traceId = MDC.get("traceId");
            }
            reqCtx.setTraceId(traceId);

            // Request ID
            String requestId = request.getHeader("X-Request-Id");
            reqCtx.setRequestId(requestId);

            context.setRequest(reqCtx);
        } catch (IllegalStateException e) {
            // Not in a request-bound thread — this is normal for async/non-HTTP contexts
            log.debug("No request context available — skipping RequestContextProvider");
        }
    }

    private boolean isEmpty(String s) {
        return s == null || s.isBlank();
    }
}
