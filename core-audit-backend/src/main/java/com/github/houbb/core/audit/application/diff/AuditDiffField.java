package com.github.houbb.core.audit.application.diff;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义 Diff 字段显示名称
 * <p>当 Java 字段名和业务展示名不一致时使用。</p>
 *
 * <pre>
 * &#64;AuditDiffField("用户角色")
 * private String role;
 * </pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditDiffField {

    /** 业务展示名称，默认使用 Java 字段名 */
    String value();
}