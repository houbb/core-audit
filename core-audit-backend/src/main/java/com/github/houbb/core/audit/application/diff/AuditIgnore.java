package com.github.houbb.core.audit.application.diff;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记字段不参与 Diff 比较
 * <p>用于排除每次必然变更的字段，如 updateTime、version 等。</p>
 *
 * <pre>
 * &#64;AuditIgnore
 * private LocalDateTime updateTime;
 * </pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditIgnore {
}