package com.github.houbb.core.audit.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.intelligence.AuditPattern;
import com.github.houbb.core.audit.application.port.AuditPatternRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 模式识别引擎
 * <p>基于时间窗口统计操作人行为，识别异常模式。
 * 使用简单统计模型，不引入复杂 ML。</p>
 *
 * <p>识别维度：</p>
 * <ul>
 *   <li>操作人时间分布（如：是否在异常时间段操作）</li>
 *   <li>操作频次（如：短时间内高频操作）</li>
 *   <li>操作类型分布（如：DELETE 占比异常高）</li>
 * </ul>
 */
@Service
public class PatternEngine {

    private static final Logger log = LoggerFactory.getLogger(PatternEngine.class);

    private final AuditPatternRepository patternRepository;
    private final ObjectMapper objectMapper;

    /** 高频操作阈值：5分钟内超过 20 次操作视为异常 */
    private static final int HIGH_FREQUENCY_THRESHOLD = 20;
    private static final int HIGH_FREQUENCY_WINDOW_MINUTES = 5;

    public PatternEngine(AuditPatternRepository patternRepository) {
        this.patternRepository = patternRepository;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 分析操作人行为模式，返回异常发现
     *
     * @param event 当前审计事件
     * @return 异常描述列表（空列表 = 无异常）
     */
    public List<PatternAnomaly> detectAnomalies(AuditEvent event) {
        List<PatternAnomaly> anomalies = new ArrayList<>();

        String operatorId = event.getOperatorId();
        if (operatorId == null || operatorId.isBlank()) return anomalies;

        // 获取或创建操作人模式
        AuditPattern pattern = getOrCreateOperatorPattern(operatorId);
        PatternData data = parsePatternData(pattern.getContentJson());

        // 更新模式数据
        updatePatternData(event, data, pattern);

        // 检测异常
        // 1. 时间异常：操作发生在非典型时段
        if (data.typicalHours != null && !data.typicalHours.isEmpty()) {
            int currentHour = LocalDateTime.now().getHour();
            boolean isTypical = data.typicalHours.stream().anyMatch(
                    range -> currentHour >= range[0] && currentHour <= range[1]);
            if (!isTypical && data.sampleCount > 10) {
                anomalies.add(new PatternAnomaly(
                        "操作时间异常",
                        "操作人 " + (event.getOperatorName() != null ? event.getOperatorName() : operatorId) +
                        " 在非典型时段 " + currentHour + ":00 进行了 " + event.getAction() + " 操作",
                        "time-anomaly"));
            }
        }

        // 2. 频次异常：短时间内操作过多
        if (data.recentTimestamps != null && data.recentTimestamps.size() >= HIGH_FREQUENCY_THRESHOLD) {
            long recentCount = data.recentTimestamps.stream()
                    .filter(ts -> ts.isAfter(LocalDateTime.now().minusMinutes(HIGH_FREQUENCY_WINDOW_MINUTES)))
                    .count();
            if (recentCount >= HIGH_FREQUENCY_THRESHOLD) {
                anomalies.add(new PatternAnomaly(
                        "高频操作异常",
                        "操作人 " + (event.getOperatorName() != null ? event.getOperatorName() : operatorId) +
                        " 在 " + HIGH_FREQUENCY_WINDOW_MINUTES + " 分钟内执行了 " + recentCount + " 次操作",
                        "high-frequency"));
            }
        }

        // 3. 操作类型异常：DELETE 占比过高
        if (data.actionDistribution != null && data.sampleCount > 20) {
            long deleteCount = data.actionDistribution.getOrDefault("DELETE", 0L);
            if (deleteCount > data.sampleCount * 0.3) {
                anomalies.add(new PatternAnomaly(
                        "操作类型异常",
                        "操作人 " + (event.getOperatorName() != null ? event.getOperatorName() : operatorId) +
                        " 的 DELETE 操作占比 " + String.format("%.0f%%", 100.0 * deleteCount / data.sampleCount) +
                        "，远超正常水平",
                        "action-distribution"));
            }
        }

        return anomalies;
    }

    /**
     * 获取或创建操作人模式
     */
    private AuditPattern getOrCreateOperatorPattern(String operatorId) {
        return patternRepository.findByTypeAndOwner("OPERATOR", operatorId)
                .orElseGet(() -> {
                    AuditPattern newPattern = AuditPattern.builder()
                            .type("OPERATOR")
                            .owner(operatorId)
                            .contentJson("{}")
                            .confidence(0.0)
                            .sampleCount(0)
                            .build();
                    return patternRepository.save(newPattern);
                });
    }

    /**
     * 更新模式数据
     */
    private void updatePatternData(AuditEvent event, PatternData data, AuditPattern pattern) {
        data.sampleCount = (data.sampleCount != null ? data.sampleCount : 0) + 1;
        pattern.setSampleCount(data.sampleCount);

        // 更新时间戳列表（保留最近 100 条）
        if (data.recentTimestamps == null) data.recentTimestamps = new ArrayList<>();
        data.recentTimestamps.add(LocalDateTime.now());
        if (data.recentTimestamps.size() > 100) {
            data.recentTimestamps = new ArrayList<>(data.recentTimestamps.subList(
                    data.recentTimestamps.size() - 100, data.recentTimestamps.size()));
        }

        // 更新操作分布
        if (data.actionDistribution == null) data.actionDistribution = new HashMap<>();
        String actionName = event.getAction().name();
        data.actionDistribution.merge(actionName, 1L, Long::sum);

        // 更新典型时段（简化：取 09:00-18:00）
        if (data.typicalHours == null) data.typicalHours = List.of(new int[]{9, 18});

        // 更新置信度
        pattern.setConfidence(Math.min(0.95, data.sampleCount / 100.0));

        // 保存
        try {
            pattern.setContentJson(objectMapper.writeValueAsString(data));
            patternRepository.save(pattern);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize pattern data: {}", e.getMessage());
        }
    }

    /**
     * 解析模式数据 JSON
     */
    private PatternData parsePatternData(String json) {
        if (json == null || json.isBlank() || "{}".equals(json)) {
            return new PatternData();
        }
        try {
            return objectMapper.readValue(json, PatternData.class);
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse pattern data: {}", e.getMessage());
            return new PatternData();
        }
    }

    /**
     * 模式数据内部结构
     */
    @SuppressWarnings("unused")
    private static class PatternData {
        public Integer sampleCount;
        public List<LocalDateTime> recentTimestamps;
        public Map<String, Long> actionDistribution;
        public List<int[]> typicalHours;
    }

    /**
     * 模式异常发现
     */
    public static class PatternAnomaly {
        private final String title;
        private final String description;
        private final String anomalyType;

        public PatternAnomaly(String title, String description, String anomalyType) {
            this.title = title;
            this.description = description;
            this.anomalyType = anomalyType;
        }

        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getAnomalyType() { return anomalyType; }
    }
}