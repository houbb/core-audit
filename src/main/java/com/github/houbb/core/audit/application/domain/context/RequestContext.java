package com.github.houbb.core.audit.application.domain.context;

/**
 * 请求上下文 — 表示"从哪里来"
 * <p>由 RequestContextProvider 从 HTTP 请求自动填充。</p>
 */
public class RequestContext {

    private String requestUri;
    private String method;
    private String traceId;
    private String requestId;
    private String referer;
    private String userAgent;
    private String host;
    private String protocol;

    // ======== Constructors ========

    public RequestContext() {
    }

    // ======== Getters & Setters ========

    public String getRequestUri() { return requestUri; }
    public void setRequestUri(String requestUri) { this.requestUri = requestUri; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getReferer() { return referer; }
    public void setReferer(String referer) { this.referer = referer; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    public String getProtocol() { return protocol; }
    public void setProtocol(String protocol) { this.protocol = protocol; }
}
