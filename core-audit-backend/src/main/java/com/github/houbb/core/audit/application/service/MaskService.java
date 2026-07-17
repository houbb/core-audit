package com.github.houbb.core.audit.application.service;

import com.github.houbb.core.audit.api.response.AuditEventResponse;
import com.github.houbb.core.audit.application.compliance.AuditMask;
import com.github.houbb.core.audit.application.compliance.MaskStrategy;
import com.github.houbb.core.audit.application.domain.enums.SensitiveType;
import com.github.houbb.core.audit.infrastructure.config.AuditProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 脱敏服务 — 自动对 API 响应中的敏感字段进行脱敏
 * <p>脱敏发生在展示层（AuditEventResponse），原始数据在 DB 中保持不变。</p>
 *
 * <p>脱敏优先级：</p>
 * <ol>
 *   <li>字段上有 @AuditMask(type = ...) 注解 → 使用指定类型脱敏</li>
 *   <li>字段上没有注解 → 通过 MaskStrategy.detectByFieldName() 试探</li>
 *   <li>都没有匹配 → 不脱敏</li>
 * </ol>
 */
@Service
public class MaskService {

    private static final Logger log = LoggerFactory.getLogger(MaskService.class);

    private final List<MaskStrategy> strategies;
    private final AuditProperties auditProperties;

    /** 字段名 → SensitiveType 缓存，避免重复反射 */
    private final Map<String, SensitiveType> fieldCache = new HashMap<>();

    public MaskService(List<MaskStrategy> strategies, AuditProperties auditProperties) {
        this.strategies = strategies != null ? strategies : Collections.emptyList();
        this.auditProperties = auditProperties;
        // 按 order 排序
        this.strategies.sort(Comparator.comparingInt(MaskStrategy::order));
        log.info("MaskService initialized with {} strategy(s)", this.strategies.size());
    }

    /**
     * 对 AuditEventResponse 中的敏感字段进行脱敏
     * <p>使用反射扫描所有 String 字段，根据 @AuditMask 注解或字段名检测应用脱敏。</p>
     *
     * @param response 审计事件响应
     */
    public void applyMask(AuditEventResponse response) {
        if (response == null) return;
        if (!auditProperties.getCompliance().getMask().isEnabled()) return;

        try {
            Field[] fields = response.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.getType() != String.class) continue;

                // 1. 确定敏感类型
                SensitiveType type = resolveSensitiveType(field);
                if (type == null) continue;

                // 2. 获取原始值
                field.setAccessible(true);
                String originalValue = (String) field.get(response);
                if (originalValue == null || originalValue.isBlank()) continue;

                // 3. 应用脱敏
                String maskedValue = mask(originalValue, type);
                field.set(response, maskedValue);
            }
        } catch (Exception e) {
            log.warn("Mask application failed: {}", e.getMessage());
            // 故障隔离 — 脱敏失败不影响 API 响应
        }
    }

    /**
     * 获取 MaskService 单例（供 AuditEventResponse.from() 静态方法使用）
     * <p>通过 MaskServiceHolder 持有，避免改动 from() 方法签名。</p>
     */
    public static MaskService getInstance() {
        return MaskServiceHolder.INSTANCE;
    }

    /**
     * 对指定值按类型脱敏
     */
    public String mask(String value, SensitiveType type) {
        for (MaskStrategy strategy : strategies) {
            String masked = strategy.mask(value, type);
            if (masked != null && !masked.equals(value)) {
                return masked;
            }
        }
        // 如果没有策略匹配，使用内置的简单掩盖
        return "***";
    }

    /**
     * 解决字段的敏感类型
     * <p>优先 @AuditMask 注解 → 字段名检测 → null</p>
     */
    private SensitiveType resolveSensitiveType(Field field) {
        String fieldName = field.getName();

        // 1. 检查 @AuditMask 注解
        AuditMask annotation = field.getAnnotation(AuditMask.class);
        if (annotation != null) {
            return annotation.value();
        }

        // 2. 通过字段名检测
        return fieldCache.computeIfAbsent(fieldName, name -> {
            for (MaskStrategy strategy : strategies) {
                SensitiveType type = strategy.detectByFieldName(name);
                if (type != null) return type;
            }
            return null;
        });
    }

    /**
     * MaskService 持有者 — 解决静态工厂方法的 DI 问题
     */
    static final class MaskServiceHolder {
        static volatile MaskService INSTANCE;

        private MaskServiceHolder() {
        }
    }

    /**
     * Spring 初始化后自动注册
     */
    @jakarta.annotation.PostConstruct
    void register() {
        MaskServiceHolder.INSTANCE = this;
        log.debug("MaskService registered in holder");
    }
}