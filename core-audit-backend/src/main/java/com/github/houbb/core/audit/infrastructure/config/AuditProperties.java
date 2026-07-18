package com.github.houbb.core.audit.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * core-audit 自定义配置属性
 */
@Configuration
@ConfigurationProperties(prefix = "core.audit")
public class AuditProperties {

    /** SDK 配置 */
    private Sdk sdk = new Sdk();

    /** P2 Context 配置 */
    private Context context = new Context();

    /** P3 Diff 配置 */
    private Diff diff = new Diff();

    /** P6 Replay 配置 */
    private Replay replay = new Replay();

    /** P7 Compliance 配置 */
    private Compliance compliance = new Compliance();

    /** P8 Intelligence 配置 */
    private Intelligence intelligence = new Intelligence();

    /** P9 Enterprise 配置 */
    private Enterprise enterprise = new Enterprise();

    public Sdk getSdk() { return sdk; }
    public void setSdk(Sdk sdk) { this.sdk = sdk; }

    public Context getContext() { return context; }
    public void setContext(Context context) { this.context = context; }

    public Diff getDiff() { return diff; }
    public void setDiff(Diff diff) { this.diff = diff; }

    public Replay getReplay() { return replay; }
    public void setReplay(Replay replay) { this.replay = replay; }

    public Compliance getCompliance() { return compliance; }
    public void setCompliance(Compliance compliance) { this.compliance = compliance; }

    public Intelligence getIntelligence() { return intelligence; }
    public void setIntelligence(Intelligence intelligence) { this.intelligence = intelligence; }

    public Enterprise getEnterprise() { return enterprise; }
    public void setEnterprise(Enterprise enterprise) { this.enterprise = enterprise; }

    public static class Sdk {
        /** 是否启用 SDK */
        private boolean enabled = false;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    public static class Context {
        /** 是否启用上下文自动采集 */
        private boolean enabled = true;

        /** 跳过上下文采集的路径 */
        private List<String> excludedPaths = new ArrayList<>();

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public List<String> getExcludedPaths() { return excludedPaths; }
        public void setExcludedPaths(List<String> excludedPaths) { this.excludedPaths = excludedPaths; }
    }

    public static class Diff {
        /** 是否启用 Diff */
        private boolean enabled = true;

        /** 最大快照大小（字节），默认 2MB */
        private Long maxSnapshotSize = 2 * 1024 * 1024L;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public Long getMaxSnapshotSize() { return maxSnapshotSize; }
        public void setMaxSnapshotSize(Long maxSnapshotSize) { this.maxSnapshotSize = maxSnapshotSize; }
    }

    public static class Replay {
        /** 是否启用 Replay */
        private boolean enabled = true;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    public static class Compliance {
        /** 是否启用合规模块 */
        private boolean enabled = true;

        /** 完整性配置 */
        private Integrity integrity = new Integrity();

        /** 脱敏配置 */
        private Mask mask = new Mask();

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public Integrity getIntegrity() { return integrity; }
        public void setIntegrity(Integrity integrity) { this.integrity = integrity; }

        public Mask getMask() { return mask; }
        public void setMask(Mask mask) { this.mask = mask; }

        public static class Integrity {
            /** 哈希算法 */
            private String algorithm = "SHA-256";

            public String getAlgorithm() { return algorithm; }
            public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
        }

        public static class Mask {
            /** 是否启用脱敏 */
            private boolean enabled = true;

            public boolean isEnabled() { return enabled; }
            public void setEnabled(boolean enabled) { this.enabled = enabled; }
        }
    }

    // ======== P8 Intelligence ========

    public static class Intelligence {
        /** 是否启用 Intelligence */
        private boolean enabled = true;

        /** Rule Engine 配置 */
        private Rule rule = new Rule();

        /** AI 配置 */
        private Ai ai = new Ai();

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public Rule getRule() { return rule; }
        public void setRule(Rule rule) { this.rule = rule; }

        public Ai getAi() { return ai; }
        public void setAi(Ai ai) { this.ai = ai; }

        public static class Rule {
            /** 是否启用规则引擎 */
            private boolean enabled = true;

            public boolean isEnabled() { return enabled; }
            public void setEnabled(boolean enabled) { this.enabled = enabled; }
        }

        public static class Ai {
            /** 是否启用 AI 分析 */
            private boolean enabled = false;

            /** AI Provider (如 openai) */
            private String provider = "openai";

            /** 模型名称 */
            private String model = "gpt-4o-mini";

            /** API Base URL */
            private String baseUrl = "https://api.openai.com";

            /** API Key */
            private String apiKey = "";

            public boolean isEnabled() { return enabled; }
            public void setEnabled(boolean enabled) { this.enabled = enabled; }
            public String getProvider() { return provider; }
            public void setProvider(String provider) { this.provider = provider; }
            public String getModel() { return model; }
            public void setModel(String model) { this.model = model; }
            public String getBaseUrl() { return baseUrl; }
            public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
            public String getApiKey() { return apiKey; }
            public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        }
    }

    // ======== P9 Enterprise ========

    public static class Enterprise {
        /** 是否启用企业平台功能 */
        private boolean enabled = true;

        /** 默认租户 ID */
        private String defaultTenant = "default";

        /** Webhook 是否启用 */
        private boolean webhookEnabled = true;

        /** Streaming 是否启用 */
        private boolean streamingEnabled = true;

        /** 插件 Marketplace 是否启用 */
        private boolean marketplaceEnabled = true;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getDefaultTenant() { return defaultTenant; }
        public void setDefaultTenant(String defaultTenant) { this.defaultTenant = defaultTenant; }
        public boolean isWebhookEnabled() { return webhookEnabled; }
        public void setWebhookEnabled(boolean webhookEnabled) { this.webhookEnabled = webhookEnabled; }
        public boolean isStreamingEnabled() { return streamingEnabled; }
        public void setStreamingEnabled(boolean streamingEnabled) { this.streamingEnabled = streamingEnabled; }
        public boolean isMarketplaceEnabled() { return marketplaceEnabled; }
        public void setMarketplaceEnabled(boolean marketplaceEnabled) { this.marketplaceEnabled = marketplaceEnabled; }
    }
}