package com.github.houbb.core.audit.application.service;

import com.github.houbb.core.audit.application.context.ContextResolver;
import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.AuditEventPage;
import com.github.houbb.core.audit.application.domain.diff.Change;
import com.github.houbb.core.audit.application.domain.diff.ChangeSet;
import com.github.houbb.core.audit.application.domain.enums.AuditAction;
import com.github.houbb.core.audit.application.port.AuditEventRepository;
import com.github.houbb.core.audit.application.port.ChangeRepository;
import com.github.houbb.core.audit.application.query.AuditEventQuery;
import com.github.houbb.core.audit.application.domain.query.AuditQuery;
import com.github.houbb.core.audit.application.domain.query.AuditQueryResult;
import com.github.houbb.core.audit.application.query.engine.AuditQueryEngine;
import com.github.houbb.core.audit.infrastructure.csv.CsvExportUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.Map;

/**
 * 审计事件核心业务服务
 * <p>提供写入、查询、导出、统计等核心能力。</p>
 */
@Service
public class AuditEventService {

    private static final Logger log = LoggerFactory.getLogger(AuditEventService.class);

    private final AuditEventRepository repository;
    private final AuditEventPublisher publisher;
    private final ContextResolver contextResolver;
    private final DiffEngine diffEngine;
    private final SnapshotResolver snapshotResolver;
    private final ChangeRepository changeRepository;
    private final AuditQueryEngine auditQueryEngine;
    private final TimelineService timelineService;
    private final ReplayService replayService;

    public AuditEventService(AuditEventRepository repository, AuditEventPublisher publisher,
                             ContextResolver contextResolver,
                             DiffEngine diffEngine, SnapshotResolver snapshotResolver,
                             ChangeRepository changeRepository,
                             AuditQueryEngine auditQueryEngine,
                             TimelineService timelineService,
                             ReplayService replayService) {
        this.repository = repository;
        this.publisher = publisher;
        this.contextResolver = contextResolver;
        this.diffEngine = diffEngine;
        this.snapshotResolver = snapshotResolver;
        this.changeRepository = changeRepository;
        this.auditQueryEngine = auditQueryEngine;
        this.timelineService = timelineService;
        this.replayService = replayService;
    }

    /**
     * 记录一条审计事件
     *
     * @param event 审计事件
     * @return 已保存的审计事件
     */
    public AuditEvent record(AuditEvent event) {
        if (event.getId() == null || event.getId().isBlank()) {
            event.setId(UUID.randomUUID().toString());
        }
        if (event.getCreatedAt() == null) {
            event.setCreatedAt(LocalDateTime.now());
        }
        if (event.getMetadata() == null) {
            event.setMetadata(new HashMap<>());
        }

        // P1 defaults
        if (event.getEventId() == null || event.getEventId().isBlank()) {
            event.setEventId(UUID.randomUUID().toString());
        }
        if (event.getOccurredAt() == null) {
            event.setOccurredAt(LocalDateTime.now());
        }
        if (event.getVersion() == null || event.getVersion().isBlank()) {
            event.setVersion("1.0");
        }

        log.debug("Recording audit event: module={}, action={}, eventType={}, target={}:{}, result={}",
                event.getModule(), event.getAction(), event.getEventType(),
                event.getTargetType(), event.getTargetId(),
                event.getResult());

        // P2: auto-resolve context before save
        contextResolver.resolve(event);

        // P3: Diff pipeline (only for UPDATE with before/after metadata)
        performDiff(event);

        AuditEvent saved = repository.save(event);

        // P5: Timeline append (sync incremental, multi-home)
        try {
            timelineService.append(saved);
        } catch (Exception e) {
            log.warn("Timeline append failed for audit {}: {}", saved.getId(), e.getMessage());
            // Do NOT block — same fault-isolation as performDiff()
        }

        // Record First, Event Second
        publisher.publish(saved);

        return saved;
    }

    /**
     * 根据 ID 查询审计事件
     */
    public Optional<AuditEvent> getById(String id) {
        return repository.findById(id);
    }

    /**
     * 分页查询审计事件
     */
    public AuditEventPage query(AuditEventQuery query) {
        return repository.findAll(query);
    }

    /**
     * 通过 AuditQuery DSL 查询（P4 新增）
     * <p>使用 Query Engine 执行统一查询，支持 Diff 深度集成。</p>
     *
     * @param query 统一查询 DSL
     * @return 查询结果（含 Diff 变更数据）
     */
    public AuditQueryResult queryViaEngine(AuditQuery query) {
        return auditQueryEngine.execute(query);
    }

    /**
     * 导出审计事件为 CSV
     *
     * @param query       查询条件
     * @param outputStream 输出流
     */
    public void exportCsv(AuditEventQuery query, OutputStream outputStream) {
        // 导出不分页，获取全部匹配结果（最多 10000 条，防止 OOM）
        query.setPage(1);
        query.setSize(10000);
        AuditEventPage page = repository.findAll(query);
        CsvExportUtil.write(page.getItems(), outputStream);
    }

    /**
     * 获取 Dashboard 统计数据
     */
    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();
        stats.setTodayTotal(repository.countToday());
        stats.setTodaySuccess(repository.countTodaySuccess());
        stats.setTodayFail(repository.countTodayFail());
        stats.setActiveModules(repository.countActiveModulesToday());
        stats.setTodayPublished(repository.countTodayPublished());
        stats.setTodayPublishFailed(repository.countTodayPublishFailed());

        // P2 context stats
        stats.setBrowserDistribution(repository.browserDistributionToday());
        stats.setTopOperators(repository.topOperatorsToday(5));
        stats.setTopModules(repository.topModulesToday(5));
        stats.setTopOrganizations(repository.topOrganizationsToday(5));

        // P3 diff stats
        stats.setChangeTypeDistribution(changeRepository.changeTypeDistributionToday());
        stats.setTopChangedFields(changeRepository.topChangedFieldsToday(5));

        // P5 timeline stats
        stats.setTodayTimelineCount(timelineService.countToday());
        stats.setAvgTimelineLength(timelineService.avgLengthToday());
        stats.setMaxTimelineLength(timelineService.maxLengthToday());
        stats.setAvgTimelineDuration(timelineService.avgDurationToday());

        // P6 replay stats
        stats.setTodayReplayCount(replayService.countToday());
        stats.setAvgReplaySteps(replayService.avgStepsToday());
        stats.setMaxReplaySteps(replayService.maxStepsToday());
        stats.setAvgReplayDuration(replayService.avgDurationToday());

        // 最近 10 条操作
        AuditEventQuery recentQuery = new AuditEventQuery();
        recentQuery.setPage(1);
        recentQuery.setSize(10);
        AuditEventPage recentPage = repository.findAll(recentQuery);
        stats.setRecentEvents(recentPage.getItems());

        return stats;
    }

    /**
     * 获取变更列表
     *
     * @param auditId 审计事件 ID
     * @return 变更列表
     */
    public List<Change> getChangesByAuditId(String auditId) {
        return changeRepository.findByAuditId(auditId);
    }

    /**
     * 搜索变更记录
     */
    public List<Change> searchChanges(String fieldName, String afterValue, int page, int size) {
        int offset = (page - 1) * size;
        return changeRepository.search(fieldName, afterValue, size, offset);
    }

    /**
     * 搜索变更记录总数
     */
    public long countSearchChanges(String fieldName, String afterValue) {
        return changeRepository.countBySearch(fieldName, afterValue);
    }

    /**
     * P3 Diff Pipeline — 仅在 UPDATE 且有 before/after metadata 时执行
     */
    private void performDiff(AuditEvent event) {
        if (event.getAction() != AuditAction.UPDATE) {
            return;
        }
        if (event.getMetadata() == null || event.getMetadata().isEmpty()) {
            return;
        }

        Object before = event.getMetadata().get("_before");
        Object after = event.getMetadata().get("_after");

        if (before == null && after == null) {
            return;
        }

        try {
            // 生成并保存快照
            if (before != null) {
                snapshotResolver.captureBefore(event.getId(), before);
            }
            if (after != null) {
                snapshotResolver.captureAfter(event.getId(), after);
            }

            // 生成并保存变更
            ChangeSet changeSet = diffEngine.diff(
                    event.getTargetType(), event.getTargetId(),
                    before, after, event.getOperatorName());

            if (changeSet.hasChanges()) {
                changeRepository.saveAll(event.getId(), changeSet.getChanges());
                log.debug("Diff recorded for audit {}: {} changes ({} total fields)",
                        event.getId(), changeSet.changedCount(), changeSet.getChanges().size());
            }
        } catch (Exception e) {
            log.error("Diff pipeline failed for audit {}: {}", event.getId(), e.getMessage());
            // Diff failure does NOT block audit recording
        }
    }

    /**
     * Dashboard 统计数据
     */
    public static class DashboardStats {
        private long todayTotal;
        private long todaySuccess;
        private long todayFail;
        private int activeModules;
        private long todayPublished;
        private long todayPublishFailed;
        private Map<String, Long> browserDistribution = Collections.emptyMap();
        private List<Map<String, Object>> topOperators = Collections.emptyList();
        private List<Map<String, Object>> topModules = Collections.emptyList();
        private List<Map<String, Object>> topOrganizations = Collections.emptyList();
        private List<AuditEvent> recentEvents = Collections.emptyList();
        // P3 diff stats
        private Map<String, Long> changeTypeDistribution = Collections.emptyMap();
        private List<Map<String, Object>> topChangedFields = Collections.emptyList();
        // P5 timeline stats
        private long todayTimelineCount;
        private double avgTimelineLength;
        private int maxTimelineLength;
        private double avgTimelineDuration;

        // P6 replay stats
        private long todayReplayCount;
        private double avgReplaySteps;
        private int maxReplaySteps;
        private double avgReplayDuration;

        public long getTodayTotal() { return todayTotal; }
        public void setTodayTotal(long todayTotal) { this.todayTotal = todayTotal; }
        public long getTodaySuccess() { return todaySuccess; }
        public void setTodaySuccess(long todaySuccess) { this.todaySuccess = todaySuccess; }
        public long getTodayFail() { return todayFail; }
        public void setTodayFail(long todayFail) { this.todayFail = todayFail; }
        public int getActiveModules() { return activeModules; }
        public void setActiveModules(int activeModules) { this.activeModules = activeModules; }
        public long getTodayPublished() { return todayPublished; }
        public void setTodayPublished(long todayPublished) { this.todayPublished = todayPublished; }
        public long getTodayPublishFailed() { return todayPublishFailed; }
        public void setTodayPublishFailed(long todayPublishFailed) { this.todayPublishFailed = todayPublishFailed; }
        public Map<String, Long> getBrowserDistribution() { return browserDistribution; }
        public void setBrowserDistribution(Map<String, Long> browserDistribution) { this.browserDistribution = browserDistribution; }
        public List<Map<String, Object>> getTopOperators() { return topOperators; }
        public void setTopOperators(List<Map<String, Object>> topOperators) { this.topOperators = topOperators; }
        public List<Map<String, Object>> getTopModules() { return topModules; }
        public void setTopModules(List<Map<String, Object>> topModules) { this.topModules = topModules; }
        public List<Map<String, Object>> getTopOrganizations() { return topOrganizations; }
        public void setTopOrganizations(List<Map<String, Object>> topOrganizations) { this.topOrganizations = topOrganizations; }
        public List<AuditEvent> getRecentEvents() { return recentEvents; }
        public void setRecentEvents(List<AuditEvent> recentEvents) { this.recentEvents = recentEvents; }
    public Map<String, Long> getChangeTypeDistribution() { return changeTypeDistribution; }
        public void setChangeTypeDistribution(Map<String, Long> changeTypeDistribution) { this.changeTypeDistribution = changeTypeDistribution; }
        public List<Map<String, Object>> getTopChangedFields() { return topChangedFields; }
        public void setTopChangedFields(List<Map<String, Object>> topChangedFields) { this.topChangedFields = topChangedFields; }
        public long getTodayTimelineCount() { return todayTimelineCount; }
        public void setTodayTimelineCount(long todayTimelineCount) { this.todayTimelineCount = todayTimelineCount; }
        public double getAvgTimelineLength() { return avgTimelineLength; }
        public void setAvgTimelineLength(double avgTimelineLength) { this.avgTimelineLength = avgTimelineLength; }
        public int getMaxTimelineLength() { return maxTimelineLength; }
        public void setMaxTimelineLength(int maxTimelineLength) { this.maxTimelineLength = maxTimelineLength; }
        public double getAvgTimelineDuration() { return avgTimelineDuration; }
        public void setAvgTimelineDuration(double avgTimelineDuration) { this.avgTimelineDuration = avgTimelineDuration; }
        public long getTodayReplayCount() { return todayReplayCount; }
        public void setTodayReplayCount(long todayReplayCount) { this.todayReplayCount = todayReplayCount; }
        public double getAvgReplaySteps() { return avgReplaySteps; }
        public void setAvgReplaySteps(double avgReplaySteps) { this.avgReplaySteps = avgReplaySteps; }
        public int getMaxReplaySteps() { return maxReplaySteps; }
        public void setMaxReplaySteps(int maxReplaySteps) { this.maxReplaySteps = maxReplaySteps; }
        public double getAvgReplayDuration() { return avgReplayDuration; }
        public void setAvgReplayDuration(double avgReplayDuration) { this.avgReplayDuration = avgReplayDuration; }
    }
}