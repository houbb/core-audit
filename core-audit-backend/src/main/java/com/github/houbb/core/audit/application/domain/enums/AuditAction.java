package com.github.houbb.core.audit.application.domain.enums;

/**
 * 审计操作枚举
 * <p>统一操作类型，避免 CREATE/delete/DeleteUser 等不一致问题。</p>
 */
public enum AuditAction {

    /** 创建 */
    CREATE,

    /** 更新 */
    UPDATE,

    /** 删除 */
    DELETE,

    /** 登录 */
    LOGIN,

    /** 登出 */
    LOGOUT,

    /** 上传 */
    UPLOAD,

    /** 下载 */
    DOWNLOAD,

    /** 导出 */
    EXPORT,

    /** 导入 */
    IMPORT,

    /** 启用 */
    ENABLE,

    /** 禁用 */
    DISABLE,

    /** 审批通过 */
    APPROVE,

    /** 审批拒绝 */
    REJECT,

    /** 执行 */
    EXECUTE,

    /** 调用 */
    CALL
}