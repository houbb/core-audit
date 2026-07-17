package com.github.houbb.core.audit.api.request;

import com.github.houbb.core.audit.application.domain.enums.AuditModule;
import com.github.houbb.core.audit.application.domain.enums.ExportFormat;

/**
 * 合规导出请求
 */
public class ExportRequest {

    private String module;
    private String action;
    private String startTime;
    private String endTime;
    private String keyword;
    private ExportFormat format = ExportFormat.CSV;
    private boolean maskEnabled = true;
    private boolean includeDiff;
    private boolean includeTimeline;

    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public ExportFormat getFormat() { return format; }
    public void setFormat(ExportFormat format) { this.format = format; }
    public boolean isMaskEnabled() { return maskEnabled; }
    public void setMaskEnabled(boolean maskEnabled) { this.maskEnabled = maskEnabled; }
    public boolean isIncludeDiff() { return includeDiff; }
    public void setIncludeDiff(boolean includeDiff) { this.includeDiff = includeDiff; }
    public boolean isIncludeTimeline() { return includeTimeline; }
    public void setIncludeTimeline(boolean includeTimeline) { this.includeTimeline = includeTimeline; }
}