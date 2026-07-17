package com.github.houbb.core.audit.api.response;

import com.github.houbb.core.audit.application.domain.compliance.ExportTask;
import com.github.houbb.core.audit.application.domain.enums.ExportFormat;
import com.github.houbb.core.audit.application.domain.enums.ExportStatus;

import java.time.LocalDateTime;

/**
 * 导出任务响应
 */
public class ExportTaskResponse {

    private String id;
    private ExportFormat format;
    private ExportStatus status;
    private boolean maskEnabled;
    private boolean includeDiff;
    private boolean includeTimeline;
    private String filePath;
    private String errorMessage;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public static ExportTaskResponse from(ExportTask task) {
        ExportTaskResponse resp = new ExportTaskResponse();
        resp.id = task.getId();
        resp.format = task.getFormat();
        resp.status = task.getStatus();
        resp.maskEnabled = task.isMaskEnabled();
        resp.includeDiff = task.isIncludeDiff();
        resp.includeTimeline = task.isIncludeTimeline();
        resp.filePath = task.getFilePath();
        resp.errorMessage = task.getErrorMessage();
        resp.createdBy = task.getCreatedBy();
        resp.createdAt = task.getCreatedAt();
        resp.completedAt = task.getCompletedAt();
        return resp;
    }

    public String getId() { return id; }
    public ExportFormat getFormat() { return format; }
    public ExportStatus getStatus() { return status; }
    public boolean isMaskEnabled() { return maskEnabled; }
    public boolean isIncludeDiff() { return includeDiff; }
    public boolean isIncludeTimeline() { return includeTimeline; }
    public String getFilePath() { return filePath; }
    public String getErrorMessage() { return errorMessage; }
    public String getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
}