package com.github.houbb.core.audit.application.domain.enums;

/**
 * 审计模块枚举
 * <p>所有 Core 模块统一使用此枚举，禁止自定义字符串。</p>
 */
public enum AuditModule {

    /** 用户模块 */
    USER,

    /** 配置模块 */
    CONFIG,

    /** AI 模块 */
    AI,

    /** 文件模块 */
    FILE,

    /** 存储模块 */
    STORAGE,

    /** 工作流模块 */
    WORKFLOW,

    /** OpenAPI 模块 */
    OPENAPI,

    /** 通知模块 */
    NOTIFICATION,

    /** 计费模块 */
    BILLING
}