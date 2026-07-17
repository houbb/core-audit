package com.github.houbb.core.audit.application.domain.context;

/**
 * 审计上下文 — 统一上下文聚合对象
 * <p>聚合 5 个子上下文：Operator / Request / Client / Business / System。</p>
 * <p>由 ContextResolver 自动填充后挂载到 AuditEvent 上。</p>
 */
public class AuditContext {

    private OperatorContext operator;
    private RequestContext request;
    private ClientContext client;
    private BusinessContext business;
    private SystemContext system;

    // ======== Constructors ========

    public AuditContext() {
    }

    private AuditContext(Builder builder) {
        this.operator = builder.operator;
        this.request = builder.request;
        this.client = builder.client;
        this.business = builder.business;
        this.system = builder.system;
    }

    /**
     * 创建一个所有子上下文均为空对象的 AuditContext
     */
    public static AuditContext empty() {
        return new AuditContext();
    }

    // ======== Builder ========

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private OperatorContext operator;
        private RequestContext request;
        private ClientContext client;
        private BusinessContext business;
        private SystemContext system;

        public Builder operator(OperatorContext operator) { this.operator = operator; return this; }
        public Builder request(RequestContext request) { this.request = request; return this; }
        public Builder client(ClientContext client) { this.client = client; return this; }
        public Builder business(BusinessContext business) { this.business = business; return this; }
        public Builder system(SystemContext system) { this.system = system; return this; }

        public AuditContext build() {
            return new AuditContext(this);
        }
    }

    // ======== Getters & Setters ========

    public OperatorContext getOperator() { return operator; }
    public void setOperator(OperatorContext operator) { this.operator = operator; }
    public RequestContext getRequest() { return request; }
    public void setRequest(RequestContext request) { this.request = request; }
    public ClientContext getClient() { return client; }
    public void setClient(ClientContext client) { this.client = client; }
    public BusinessContext getBusiness() { return business; }
    public void setBusiness(BusinessContext business) { this.business = business; }
    public SystemContext getSystem() { return system; }
    public void setSystem(SystemContext system) { this.system = system; }
}
