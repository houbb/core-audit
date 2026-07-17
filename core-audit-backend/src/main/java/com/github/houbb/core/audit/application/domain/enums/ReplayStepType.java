package com.github.houbb.core.audit.application.domain.enums;

/**
 * Replay 步骤类型枚举
 * <p>P6 新增：定义操作重放的步骤类型，覆盖完整的操作过程。</p>
 *
 * <p>LOGIN — 登录步骤</p>
 * <p>OPEN_PAGE — 打开页面</p>
 * <p>CLICK_BUTTON — 点击按钮</p>
 * <p>INPUT — 输入操作</p>
 * <p>REQUEST — HTTP 请求</p>
 * <p>DATABASE — 数据库操作</p>
 * <p>AUDIT — 审计记录</p>
 * <p>EVENT — 业务事件</p>
 * <p>FINISH — 结束标记</p>
 */
public enum ReplayStepType {

    LOGIN,

    OPEN_PAGE,

    CLICK_BUTTON,

    INPUT,

    REQUEST,

    DATABASE,

    AUDIT,

    EVENT,

    FINISH

}