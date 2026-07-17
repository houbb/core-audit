package com.github.houbb.core.audit.application.compliance;

import com.github.houbb.core.audit.application.domain.enums.SensitiveType;

/**
 * 脱敏策略（SPI 扩展点）
 * <p>支持多种敏感数据类型的脱敏处理，
 * 及根据字段名称自动检测敏感类型。</p>
 */
public interface MaskStrategy {

    /**
     * 对敏感数据进行脱敏
     *
     * @param value 原始值
     * @param type  敏感数据类型
     * @return 脱敏后的值
     */
    String mask(String value, SensitiveType type);

    /**
     * 根据字段名称检测可能的敏感类型
     * <p>用于没有显式标注 @AuditMask 注解的字段。</p>
     *
     * @param fieldName Java 字段名
     * @return 检测到的敏感类型，null 表示不是敏感字段
     */
    SensitiveType detectByFieldName(String fieldName);

    /**
     * 优先级（越小越优先）
     *
     * @return 顺序值，默认 100
     */
    default int order() {
        return 100;
    }
}
