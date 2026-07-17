package com.github.houbb.core.audit.application.domain.context;

/**
 * 系统上下文 — 表示"在哪个节点"
 * <p>由 SystemContextProvider 从 Spring Environment 和系统属性自动填充。</p>
 */
public class SystemContext {

    private String service;
    private String node;
    private String version;
    private String hostname;
    private String cluster;
    private String instance;
    private String thread;

    // ======== Constructors ========

    public SystemContext() {
    }

    // ======== Getters & Setters ========

    public String getService() { return service; }
    public void setService(String service) { this.service = service; }
    public String getNode() { return node; }
    public void setNode(String node) { this.node = node; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getHostname() { return hostname; }
    public void setHostname(String hostname) { this.hostname = hostname; }
    public String getCluster() { return cluster; }
    public void setCluster(String cluster) { this.cluster = cluster; }
    public String getInstance() { return instance; }
    public void setInstance(String instance) { this.instance = instance; }
    public String getThread() { return thread; }
    public void setThread(String thread) { this.thread = thread; }
}
