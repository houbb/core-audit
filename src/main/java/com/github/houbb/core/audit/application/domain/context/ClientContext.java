package com.github.houbb.core.audit.application.domain.context;

/**
 * 客户端上下文 — 表示"用什么设备"
 * <p>由 ClientContextProvider 从 HTTP 请求头自动解析。</p>
 * <p>P2 不做 Geo IP（留到 P8），region 字段暂时为空。</p>
 */
public class ClientContext {

    private String ip;
    private String region;
    private String browser;
    private String os;
    private String device;
    private String language;
    private String timezone;

    // ======== Constructors ========

    public ClientContext() {
    }

    // ======== Getters & Setters ========

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getBrowser() { return browser; }
    public void setBrowser(String browser) { this.browser = browser; }
    public String getOs() { return os; }
    public void setOs(String os) { this.os = os; }
    public String getDevice() { return device; }
    public void setDevice(String device) { this.device = device; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
}
