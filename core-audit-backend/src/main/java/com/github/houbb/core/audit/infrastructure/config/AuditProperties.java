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

    public Sdk getSdk() { return sdk; }
    public void setSdk(Sdk sdk) { this.sdk = sdk; }

    public Context getContext() { return context; }
    public void setContext(Context context) { this.context = context; }

    public Diff getDiff() { return diff; }
    public void setDiff(Diff diff) { this.diff = diff; }

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
}