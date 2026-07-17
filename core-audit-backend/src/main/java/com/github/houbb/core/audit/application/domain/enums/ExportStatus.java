package com.github.houbb.core.audit.application.domain.enums;

/**
 * 导出任务状态
 */
public enum ExportStatus {

    /** 待处理 */
    PENDING,

    /** 处理中 */
    PROCESSING,

    /** 已完成 */
    COMPLETED,

    /** 失败 */
    FAILED
}