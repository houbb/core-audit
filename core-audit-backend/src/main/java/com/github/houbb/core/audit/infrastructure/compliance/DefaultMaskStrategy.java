package com.github.houbb.core.audit.infrastructure.compliance;

import com.github.houbb.core.audit.application.compliance.MaskStrategy;
import com.github.houbb.core.audit.application.domain.enums.SensitiveType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 默认脱敏策略实现
 * <p>内置所有 SensitiveType 的脱敏逻辑：
 * 手机号、邮箱、Token、密码、密钥、API Key。</p>
 */
@Component
public class DefaultMaskStrategy implements MaskStrategy {

    private static final Logger log = LoggerFactory.getLogger(DefaultMaskStrategy.class);

    @Override
    public String mask(String value, SensitiveType type) {
        if (value == null || value.isBlank()) {
            return value;
        }

        try {
            return switch (type) {
                case PHONE -> maskPhone(value);
                case EMAIL -> maskEmail(value);
                case TOKEN -> maskToken(value);
                case PASSWORD -> "********";
                case SECRET -> maskSecret(value);
                case API_KEY -> maskApiKey(value);
            };
        } catch (Exception e) {
            log.warn("Mask failed for type {}: {}", type, e.getMessage());
            return value;
        }
    }

    @Override
    public SensitiveType detectByFieldName(String fieldName) {
        if (fieldName == null) return null;
        String lower = fieldName.toLowerCase();
        if (lower.contains("phone") || lower.contains("mobile") || lower.contains("tel")) {
            return SensitiveType.PHONE;
        }
        if (lower.contains("email") || lower.contains("mail")) {
            return SensitiveType.EMAIL;
        }
        if (lower.contains("password") || lower.contains("pwd") || lower.contains("passwd")) {
            return SensitiveType.PASSWORD;
        }
        if (lower.contains("secret")) {
            return SensitiveType.SECRET;
        }
        if (lower.contains("token")) {
            return SensitiveType.TOKEN;
        }
        if (lower.contains("apikey") || lower.contains("api_key") || lower.contains("apikey")) {
            return SensitiveType.API_KEY;
        }
        return null;
    }

    @Override
    public int order() {
        return 100;
    }

    // ======== Private mask implementations ========

    /**
     * 手机号：保留前 3 后 4，中间用 **** 填充
     * 13812345678 → 138****5678
     */
    private String maskPhone(String value) {
        if (value.length() <= 7) {
            return value.charAt(0) + "****" + value.charAt(value.length() - 1);
        }
        return value.substring(0, 3) + "****" + value.substring(value.length() - 4);
    }

    /**
     * 邮箱：local-part 前 2 位 + *** + @domain
     * echo@example.com → ec***@example.com
     */
    private String maskEmail(String value) {
        int atIndex = value.indexOf('@');
        if (atIndex <= 0) {
            return "***";
        }
        String localPart = value.substring(0, atIndex);
        String domain = value.substring(atIndex);
        if (localPart.length() <= 2) {
            return localPart.charAt(0) + "***" + domain;
        }
        return localPart.substring(0, 2) + "***" + domain;
    }

    /**
     * Token：保留前 4 后 4，中间用 ****
     * eyJhbGciOiJIUzI1NiJ9... → eyJh****NiJ9
     */
    private String maskToken(String value) {
        if (value.length() <= 8) {
            return value.substring(0, Math.min(2, value.length())) + "***";
        }
        return value.substring(0, 4) + "****" + value.substring(value.length() - 4);
    }

    /**
     * Secret：保留前 2 后 2，中间用 ****
     */
    private String maskSecret(String value) {
        if (value.length() <= 4) {
            return "****";
        }
        return value.substring(0, 2) + "****" + value.substring(value.length() - 2);
    }

    /**
     * API Key：保留前 4 后 4，中间用 ****
     */
    private String maskApiKey(String value) {
        if (value.length() <= 8) {
            return value.substring(0, Math.min(2, value.length())) + "***";
        }
        return value.substring(0, 4) + "****" + value.substring(value.length() - 4);
    }
}