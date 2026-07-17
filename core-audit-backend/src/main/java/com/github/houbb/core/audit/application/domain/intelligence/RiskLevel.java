package com.github.houbb.core.audit.application.domain.intelligence;

/**
 * 风险等级枚举
 */
public enum RiskLevel {

    /** 低风险 — 常规操作 */
    LOW,

    /** 中风险 — 需要关注 */
    MEDIUM,

    /** 高风险 — 可能存在异常 */
    HIGH,

    /** 严重 — 立即处理 */
    CRITICAL;

    /**
     * 根据评分转换等级
     * <p>0-20: LOW, 21-50: MEDIUM, 51-80: HIGH, 81-100: CRITICAL</p>
     */
    public static RiskLevel fromScore(int score) {
        if (score <= 20) return LOW;
        if (score <= 50) return MEDIUM;
        if (score <= 80) return HIGH;
        return CRITICAL;
    }
}