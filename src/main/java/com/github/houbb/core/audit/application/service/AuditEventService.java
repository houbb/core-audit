package com.github.houbb.core.audit.application.service;

import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.AuditEventPage;
import com.github.houbb.core.audit.application.port.AuditEventRepository;
import com.github.houbb.core.audit.application.query.AuditEventQuery;
import com.github.houbb.core.audit.infrastructure.csv.CsvExportUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 审计事件核心业务服务
 * <p>提供写入、查询、导出、统计等核心能力。</p>
 */
@Service
public class AuditEventService {

    private static final Logger log = LoggerFactory.getLogger(AuditEventService.class);

    private final AuditEventRepository repository;

    public AuditEventService(AuditEventRepository repository) {
        this.repository = repository;
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

        log.debug("Recording audit event: module={}, action={}, target={}:{}, result={}",
                event.getModule(), event.getAction(),
                event.getTargetType(), event.getTargetId(),
                event.getResult());

        return repository.save(event);
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

        // 最近 10 条操作
        AuditEventQuery recentQuery = new AuditEventQuery();
        recentQuery.setPage(1);
        recentQuery.setSize(10);
        AuditEventPage recentPage = repository.findAll(recentQuery);
        stats.setRecentEvents(recentPage.getItems());

        return stats;
    }

    /**
     * Dashboard 统计数据
     */
    public static class DashboardStats {
        private long todayTotal;
        private long todaySuccess;
        private long todayFail;
        private int activeModules;
        private List<AuditEvent> recentEvents = Collections.emptyList();

        public long getTodayTotal() { return todayTotal; }
        public void setTodayTotal(long todayTotal) { this.todayTotal = todayTotal; }
        public long getTodaySuccess() { return todaySuccess; }
        public void setTodaySuccess(long todaySuccess) { this.todaySuccess = todaySuccess; }
        public long getTodayFail() { return todayFail; }
        public void setTodayFail(long todayFail) { this.todayFail = todayFail; }
        public int getActiveModules() { return activeModules; }
        public void setActiveModules(int activeModules) { this.activeModules = activeModules; }
        public List<AuditEvent> getRecentEvents() { return recentEvents; }
        public void setRecentEvents(List<AuditEvent> recentEvents) { this.recentEvents = recentEvents; }
    }
}