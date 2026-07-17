package com.github.houbb.core.audit.application.compliance;

import com.github.houbb.core.audit.application.domain.enums.SensitiveType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记字段为敏感数据，需要在 API 响应中自动脱敏
 * <p>与 {@link com.github.houbb.core.audit.application.diff.AuditIgnore}
 * 属于同一模式，但作用于展示层而非 Diff 层。</p>
 *
 * <pre>
 * &#64;AuditMask(type = SensitiveType.PHONE)
 * private String phone;
 * </pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditMask {

    /** 敏感数据类型 */
    SensitiveType value() default SensitiveType.PHONE;
}