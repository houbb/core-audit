package com.github.houbb.core.audit.application.domain.compliance;

import com.github.houbb.core.audit.application.domain.enums.ExportFormat;
import com.github.houbb.core.audit.application.domain.enums.ExportStatus;

import java.time.LocalDateTime;

/**
 * 合规导出任务
 * <p>异步导出审计数据，支持 CSV / Excel / PDF 格式。</p>
 */
public class ExportTask {

    private String id;
    private String queryJson;
    private ExportFormat format;
    private boolean maskEnabled;
    private boolean includeDiff;
    private boolean includeTimeline;
    private ExportStatus status;
    private String filePath;
    private String errorMessage;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public ExportTask() {
    }

    private ExportTask(Builder builder) {
        this.id = builder.id;
        this.queryJson = builder.queryJson;
        this.format = builder.format;
        this.maskEnabled = builder.maskEnabled;
        this.includeDiff = builder.includeDiff;
        this.includeTimeline = builder.includeTimeline;
        this.status = builder.status;
        this.filePath = builder.filePath;
        this.errorMessage = builder.errorMessage;
        this.createdBy = builder.createdBy;
        this.createdAt = builder.createdAt;
        this.completedAt = builder.completedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private String queryJson;
        private ExportFormat format = ExportFormat.CSV;
        private boolean maskEnabled = true;
        private boolean includeDiff;
        private boolean includeTimeline;
        private ExportStatus status = ExportStatus.PENDING;
        private String filePath;
        private String errorMessage;
        private String createdBy;
        private LocalDateTime createdAt;
        private LocalDateTime completedAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder queryJson(String queryJson) { this.queryJson = queryJson; return this; }
        public Builder format(ExportFormat format) { this.format = format; return this; }
        public Builder maskEnabled(boolean maskEnabled) { this.maskEnabled = maskEnabled; return this; }
        public Builder includeDiff(boolean includeDiff) { this.includeDiff = includeDiff; return this; }
        public Builder includeTimeline(boolean includeTimeline) { this.includeTimeline = includeTimeline; return this; }
        public Builder status(ExportStatus status) { this.status = status; return this; }
        public Builder filePath(String filePath) { this.filePath = filePath; return this; }
        public Builder errorMessage(String errorMessage) { this.errorMessage = errorMessage; return this; }
        public Builder createdBy(String createdBy) { this.createdBy = createdBy; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder completedAt(LocalDateTime completedAt) { this.completedAt = completedAt; return this; }

        public ExportTask build() {
            return new ExportTask(this);
        }
    }

    // ======== Getters & Setters ========

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getQueryJson() { return queryJson; }
    public void setQueryJson(String queryJson) { this.queryJson = queryJson; }
    public ExportFormat getFormat() { return format; }
    public void setFormat(ExportFormat format) { this.format = format; }
    public boolean isMaskEnabled() { return maskEnabled; }
    public void setMaskEnabled(boolean maskEnabled) { this.maskEnabled = maskEnabled; }
    public boolean isIncludeDiff() { return includeDiff; }
    public void setIncludeDiff(boolean includeDiff) { this.includeDiff = includeDiff; }
    public boolean isIncludeTimeline() { return includeTimeline; }
    public void setIncludeTimeline(boolean includeTimeline) { this.includeTimeline = includeTimeline; }
    public ExportStatus getStatus() { return status; }
    public void setStatus(ExportStatus status) { this.status = status; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
