package com.github.houbb.core.audit.application.domain.enums;

/**
 * 敏感数据类型
 * <p>用于 {@link com.github.houbb.core.audit.application.compliance.AuditMask}
 * 注解标记需要脱敏的字段。</p>
 */
public enum SensitiveType {

    /** 手机号：138****5678 */
    PHONE,

    /** 邮箱：ec***@example.com */
    EMAIL,

    /** Token：截取前 8 位 */
    TOKEN,

    /** 密码：全部替换为 ****** */
    PASSWORD,

    /** 密钥/Secret：截取前 4 位 */
    SECRET,

    /** API Key：截取前 6 位 */
    API_KEY
}