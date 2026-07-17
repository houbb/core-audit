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
}