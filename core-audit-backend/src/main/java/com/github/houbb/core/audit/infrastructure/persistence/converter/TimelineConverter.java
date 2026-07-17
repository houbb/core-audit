package com.github.houbb.core.audit.infrastructure.persistence.converter;

import com.github.houbb.core.audit.application.domain.enums.TimelineType;
import com.github.houbb.core.audit.application.domain.timeline.Timeline;
import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditTimelineEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 时间线转换器
 * <p>静态工具类，负责 Timeline 领域对象与 AuditTimelineEntity 之间的双向转换。</p>
 * <p>遵循 AuditEventConverter / DiffConverter 的模式。</p>
 */
public final class TimelineConverter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private TimelineConverter() {
        // 工具类，禁止实例化
    }

    /**
     * 领域对象 → 实体
     */
    public static AuditTimelineEntity toEntity(Timeline domain) {
        if (domain == null) return null;
        AuditTimelineEntity entity = new AuditTimelineEntity();
        entity.setId(domain.getId());
        entity.setType(domain.getType() != null ? domain.getType().name() : null);
        entity.setTitle(domain.getTitle());
        if (domain.getStartTime() != null) {
            entity.setStartTime(domain.getStartTime().format(FORMATTER));
        }
        if (domain.getEndTime() != null) {
            entity.setEndTime(domain.getEndTime().format(FORMATTER));
        }
        if (domain.getDuration() != null) {
            entity.setDuration(String.valueOf(domain.getDuration()));
        }
        entity.setSummary(domain.getSummary());
        return entity;
    }

    /**
     * 实体 → 领域对象
     */
    public static Timeline toDomain(AuditTimelineEntity entity) {
        if (entity == null) return null;
        Timeline timeline = new Timeline();
        timeline.setId(entity.getId());
        if (entity.getType() != null && !entity.getType().isBlank()) {
            try {
                timeline.setType(TimelineType.valueOf(entity.getType()));
            } catch (IllegalArgumentException e) {
                // 未知类型忽略，保留 null
            }
        }
        timeline.setTitle(entity.getTitle());
        if (entity.getStartTime() != null && !entity.getStartTime().isBlank()) {
            try {
                timeline.setStartTime(LocalDateTime.parse(entity.getStartTime(), FORMATTER));
            } catch (Exception e) {
                // 解析失败忽略
            }
        }
        if (entity.getEndTime() != null && !entity.getEndTime().isBlank()) {
            try {
                timeline.setEndTime(LocalDateTime.parse(entity.getEndTime(), FORMATTER));
            } catch (Exception e) {
                // 解析失败忽略
            }
        }
        if (entity.getDuration() != null && !entity.getDuration().isBlank()) {
            try {
                timeline.setDuration(Long.parseLong(entity.getDuration()));
            } catch (NumberFormatException e) {
                // 解析失败忽略
            }
        }
        timeline.setSummary(entity.getSummary());
        return timeline;
    }

}
