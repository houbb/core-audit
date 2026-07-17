package com.github.houbb.core.audit.application.service;

import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.enums.TimelineType;
import com.github.houbb.core.audit.application.domain.timeline.Timeline;
import com.github.houbb.core.audit.application.domain.timeline.TimelineKey;
import com.github.houbb.core.audit.application.port.TimelineRepository;
import com.github.houbb.core.audit.application.timeline.TimelineStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 时间线核心服务
 * <p>编排 Correlation Engine + Story Builder，是 P5 的中心调度器。</p>
 *
 * <p>核心职责：</p>
 * <ul>
 *   <li>接收 AuditEvent，通过所有 TimelineStrategy 解析归属，追加到对应 Timeline</li>
 *   <li>构建 Story（排序事件 + 自动标题）</li>
 *   <li>提供 Timeline 查询能力</li>
 * </ul>
 *
 * <p>容错：单个 Strategy 失败不影响其他 Strategy，Timeline 追加失败不阻塞审计记录。</p>
 */
@Service
public class TimelineService {

    private static final Logger log = LoggerFactory.getLogger(TimelineService.class);

    private final List<TimelineStrategy> strategies;
    private final TimelineRepository timelineRepository;

    public TimelineService(List<TimelineStrategy> strategies, TimelineRepository timelineRepository) {
        this.strategies = strategies.stream()
                .sorted(Comparator.comparingInt(TimelineStrategy::order))
                .toList();
        this.timelineRepository = timelineRepository;
        log.info("TimelineService initialized with {} strategy(s): {}",
                this.strategies.size(),
                this.strategies.stream().map(s -> s.getClass().getSimpleName()).toList());
    }

    // ======== Core: Append Event to Timeline(s) ========

    /**
     * 将审计事件追加到所有匹配的时间线
     * <p>多归属：一个事件可能被多个 Strategy 匹配，从而归属到多条 Timeline。</p>
     * <p>容错：单个 Strategy 失败只打 warn 日志，继续处理下一个。</p>
     *
     * @param event 审计事件
     */
    public void append(AuditEvent event) {
        for (TimelineStrategy strategy : strategies) {
            try {
                if (!strategy.supports(event)) {
                    continue;
                }
                TimelineKey key = strategy.resolve(event);
                if (key == null) continue;

                Timeline timeline = findOrCreate(key);
                int nextSeq = timelineRepository.getMaxSequence(timeline.getId()) + 1;
                timelineRepository.appendEvent(timeline.getId(), event.getId(), nextSeq);
                updateTimelineMeta(timeline.getId(), event.getCreatedAt());

                log.debug("Event {} appended to timeline {} (type={}, key={}, seq={})",
                        event.getId(), timeline.getId(), key.getType(), key.getKey(), nextSeq);
            } catch (Exception e) {
                log.warn("Timeline strategy '{}' failed for event {}: {}",
                        strategy.getClass().getSimpleName(), event.getId(), e.getMessage());
                // 故障隔离 — 单个 Strategy 失败不阻塞其他
            }
        }
    }

    // ======== Story Builder ========

    /**
     * 构建 Story：加载完整 Timeline（含排序后的 events），生成自动标题。
     *
     * @param timelineId 时间线 ID
     * @return 完整 Timeline（含 events）
     */
    public Optional<Timeline> buildStory(String timelineId) {
        Optional<Timeline> opt = timelineRepository.findById(timelineId);
        if (opt.isEmpty()) return opt;

        Timeline timeline = opt.get();
        List<AuditEvent> events = timeline.getEvents();
        if (events != null) {
            // 按 createdAt 排序（已在 repository 层按 sequence 排，这里兜底）
            // 使用 new ArrayList 避免对不可变列表排序
            events = new ArrayList<>(events);
            events.sort(Comparator.comparing(
                    e -> e.getCreatedAt() != null ? e.getCreatedAt() : LocalDateTime.MIN));
            timeline.setEvents(events);
        }

        // 自动标题：{EventCount} Events — {Type} Timeline
        int count = events != null ? events.size() : 0;
        String autoTitle = count + " Events";
        if (timeline.getStartTime() != null && timeline.getEndTime() != null) {
            autoTitle += " from " + timeline.getStartTime() + " to " + timeline.getEndTime();
        }
        if (timeline.getTitle() == null || timeline.getTitle().isBlank()) {
            timeline.setTitle(autoTitle);
        }

        return Optional.of(timeline);
    }

    // ======== Query Methods ========

    /**
     * 根据 ID 获取完整 Timeline
     */
    public Optional<Timeline> getById(String id) {
        return buildStory(id);
    }

    /**
     * 根据类型查询 Timeline 列表
     */
    public List<Timeline> getByType(TimelineType type, int limit, int offset) {
        return timelineRepository.findByType(type, limit, offset);
    }

    /**
     * 获取最近 Timeline
     */
    public List<Timeline> getRecent(int limit) {
        return timelineRepository.findRecent(limit);
    }

    /**
     * 根据对象 ID 获取 Object Timeline
     */
    public List<Timeline> getByObjectId(String targetId) {
        return timelineRepository.findByKey(TimelineKey.of("OBJECT", targetId));
    }

    /**
     * 根据操作人 ID 获取 User Timeline
     */
    public List<Timeline> getByOperatorId(String operatorId) {
        return timelineRepository.findByKey(TimelineKey.of("USER", operatorId));
    }

    /**
     * 根据 Trace ID 获取 Request Timeline
     */
    public List<Timeline> getByTraceId(String traceId) {
        return timelineRepository.findByKey(TimelineKey.of("REQUEST", traceId));
    }

    // ======== Dashboard ========

    public long countToday() { return timelineRepository.countToday(); }
    public double avgLengthToday() { return timelineRepository.avgLengthToday(); }
    public int maxLengthToday() { return timelineRepository.maxLengthToday(); }
    public double avgDurationToday() { return timelineRepository.avgDurationToday(); }

    // ======== Private Helpers ========

    /**
     * 查找或创建 Timeline
     */
    private Timeline findOrCreate(TimelineKey key) {
        List<Timeline> existing = timelineRepository.findByKey(key);
        if (!existing.isEmpty()) {
            return existing.get(0);
        }

        // 创建新 Timeline
        TimelineType type;
        try {
            type = TimelineType.valueOf(key.getType());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown TimelineType: {}, fallback to OBJECT", key.getType());
            type = TimelineType.OBJECT;
        }

        Timeline timeline = Timeline.builder()
                .id(UUID.randomUUID().toString())
                .type(type)
                .title(key.getType() + " — " + key.getKey())
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now())
                .duration(0L)
                .createdAt(LocalDateTime.now())
                .build();

        timelineRepository.save(timeline);
        log.debug("Created new timeline: id={}, type={}, key={}", timeline.getId(), key.getType(), key.getKey());
        return timeline;
    }

    /**
     * 更新 Timeline 元数据（startTime, endTime, duration）
     */
    private void updateTimelineMeta(String timelineId, LocalDateTime eventTime) {
        Optional<Timeline> opt = timelineRepository.findById(timelineId);
        if (opt.isEmpty()) return;

        Timeline timeline = opt.get();
        List<AuditEvent> events = timeline.getEvents();
        if (events == null || events.isEmpty()) return;

        // 计算最早的 startTime 和最晚的 endTime
        LocalDateTime start = events.stream()
                .map(AuditEvent::getCreatedAt)
                .filter(t -> t != null)
                .min(LocalDateTime::compareTo)
                .orElse(eventTime);

        LocalDateTime end = events.stream()
                .map(AuditEvent::getCreatedAt)
                .filter(t -> t != null)
                .max(LocalDateTime::compareTo)
                .orElse(eventTime);

        long duration = Duration.between(start, end).toMillis();

        timeline.setStartTime(start);
        timeline.setEndTime(end);
        timeline.setDuration(duration);

        timelineRepository.save(timeline);
    }
}