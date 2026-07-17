package com.github.houbb.core.audit.application.domain.context;

/**
 * 操作人上下文 — 表示"是谁"
 * <p>由 SecurityContextProvider 自动填充，无需业务手动设置。</p>
 */
public class OperatorContext {

    private String userId;
    private String username;
    private String nickname;
    private String organization;
    private String department;
    private String role;
    private String permission;
    private String tenant;
    private String loginType;

    // ======== Constructors ========

    public OperatorContext() {
    }

    // ======== Getters & Setters ========

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getPermission() { return permission; }
    public void setPermission(String permission) { this.permission = permission; }
    public String getTenant() { return tenant; }
    public void setTenant(String tenant) { this.tenant = tenant; }
    public String getLoginType() { return loginType; }
    public void setLoginType(String loginType) { this.loginType = loginType; }
}
