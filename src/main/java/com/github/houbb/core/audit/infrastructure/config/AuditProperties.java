package com.github.houbb.core.audit.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * core-audit 自定义配置属性
 */
@Configuration
@ConfigurationProperties(prefix = "core.audit")
public class AuditProperties {

    /** SDK 配置 */
    private Sdk sdk = new Sdk();

    public Sdk getSdk() { return sdk; }
    public void setSdk(Sdk sdk) { this.sdk = sdk; }

    public static class Sdk {
        /** 是否启用 SDK */
        private boolean enabled = false;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }
}