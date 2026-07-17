package com.github.houbb.core.audit.application.domain.enums;

/**
 * 审计事件类型枚举
 * <p>P1 新增：独立的业务事件类型，值采用 module_action 的语义组合。</p>
 * <p>所有模块订阅事件时使用此枚举，避免字符串混乱。</p>
 *
 * <p>命名规范：{MODULE}_{ACTION} 或 {MODULE}_{ACTION}_{SUB_ACTION}</p>
 */
public enum AuditEventType {

    // ======== User 模块事件 ========
    /** 用户创建 */
    USER_CREATED,

    /** 用户更新 */
    USER_UPDATED,

    /** 用户删除 */
    USER_DELETED,

    /** 用户登录成功 */
    USER_LOGIN_SUCCESS,

    /** 用户登录失败 */
    USER_LOGIN_FAILED,

    /** 用户登出 */
    USER_LOGOUT,

    /** 用户启用 */
    USER_ENABLED,

    /** 用户禁用 */
    USER_DISABLED,

    // ======== File 模块事件 ========
    /** 文件上传 */
    FILE_UPLOADED,

    /** 文件删除 */
    FILE_DELETED,

    /** 文件下载 */
    FILE_DOWNLOADED,

    // ======== Config 模块事件 ========
    /** 配置更新 */
    CONFIG_UPDATED,

    // ======== AI 模块事件 ========
    /** AI 请求 */
    AI_REQUESTED,

    // ======== Workflow 模块事件 ========
    /** 工作流执行 */
    WORKFLOW_EXECUTED,

    /** 工作流审批通过 */
    WORKFLOW_APPROVED,

    /** 工作流审批拒绝 */
    WORKFLOW_REJECTED,

    // ======== OpenAPI 模块事件 ========
    /** API 调用 */
    API_CALLED,

    // ======== Role & Permission 事件 ========
    /** 角色变更 */
    ROLE_CHANGED,

    // ======== Import/Export 事件 ========
    /** 数据导入 */
    DATA_IMPORTED,

    /** 数据导出 */
    DATA_EXPORTED,

    // ======== Billing 模块事件 ========
    /** 计费事件 */
    BILLING_EVENT,

    // ======== Storage 模块事件 ========
    /** 存储操作 */
    STORAGE_OPERATION,

    // ======== Notification 模块事件 ========
    /** 通知发送 */
    NOTIFICATION_SENT
}
