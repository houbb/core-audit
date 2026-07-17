package com.github.houbb.core.audit.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.houbb.core.audit.api.response.AuditEventResponse;
import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.AuditEventPage;
import com.github.houbb.core.audit.application.domain.compliance.ExportTask;
import com.github.houbb.core.audit.application.domain.enums.ExportFormat;
import com.github.houbb.core.audit.application.domain.enums.ExportStatus;
import com.github.houbb.core.audit.application.domain.query.AuditQuery;
import com.github.houbb.core.audit.application.port.*;
import com.github.houbb.core.audit.application.query.AuditEventQuery;
import com.github.houbb.core.audit.application.query.engine.AuditQueryEngine;
import com.github.houbb.core.audit.infrastructure.csv.CsvExportUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 合规导出服务 — 异步导出审计数据
 * <p>支持 CSV / Excel 格式，PDF 暂未实现。</p>
 * <p>导出任务异步执行，客户端通过 taskId 轮询状态并下载。</p>
 */
@Service
public class ExportService {

    private static final Logger log = LoggerFactory.getLogger(ExportService.class);

    private final ExportTaskRepository exportTaskRepository;
    private final AuditEventRepository auditEventRepository;
    private final AuditSignatureRepository signatureRepository;
    private final AuditQueryEngine auditQueryEngine;
    private final MaskService maskService;
    private final ObjectMapper objectMapper;

    /** 导出文件存放目录 */
    private static final String EXPORT_DIR = "./data/exports/";

    public ExportService(ExportTaskRepository exportTaskRepository,
                         AuditEventRepository auditEventRepository,
                         AuditSignatureRepository signatureRepository,
                         AuditQueryEngine auditQueryEngine,
                         MaskService maskService) {
        this.exportTaskRepository = exportTaskRepository;
        this.auditEventRepository = auditEventRepository;
        this.signatureRepository = signatureRepository;
        this.auditQueryEngine = auditQueryEngine;
        this.maskService = maskService;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 提交导出任务（立即返回 PENDING 状态，后台异步执行）
     */
    public ExportTask submitExport(AuditQuery query, ExportFormat format, boolean maskEnabled,
                                    boolean includeDiff, boolean includeTimeline, String createdBy) {
        String queryJson = serializeQuery(query);

        ExportTask task = ExportTask.builder()
                .id(UUID.randomUUID().toString())
                .queryJson(queryJson)
                .format(format != null ? format : ExportFormat.CSV)
                .maskEnabled(maskEnabled)
                .includeDiff(includeDiff)
                .includeTimeline(includeTimeline)
                .status(ExportStatus.PENDING)
                .createdBy(createdBy)
                .createdAt(LocalDateTime.now())
                .build();

        ExportTask saved = exportTaskRepository.save(task);
        log.info("Export task created: id={}, format={}, by={}", saved.getId(), saved.getFormat(), createdBy);

        // 异步执行
        executeExport(saved.getId());

        return saved;
    }

    /**
     * 查询导出任务状态
     */
    public Optional<ExportTask> getTask(String taskId) {
        return exportTaskRepository.findById(taskId);
    }

    /**
     * 获取导出文件字节
     */
    public byte[] downloadExport(String taskId) {
        Optional<ExportTask> taskOpt = exportTaskRepository.findById(taskId);
        if (taskOpt.isEmpty() || taskOpt.get().getFilePath() == null) {
            return null;
        }
        ExportTask task = taskOpt.get();
        if (task.getStatus() != ExportStatus.COMPLETED) {
            return null;
        }
        try {
            return Files.readAllBytes(Path.of(task.getFilePath()));
        } catch (IOException e) {
            log.error("Failed to read export file {}: {}", task.getFilePath(), e.getMessage());
            return null;
        }
    }

    /**
     * 获取导出历史
     */
    public List<ExportTask> getHistory(int page, int size) {
        int offset = (page - 1) * size;
        return exportTaskRepository.findHistory(size, offset);
    }

    /**
     * 今日导出数
     */
    public long countToday() {
        return exportTaskRepository.countToday();
    }

    /**
     * 清理旧导出文件
     */
    public void cleanupOldExports(int retentionDays) {
        log.info("Export cleanup: removing files older than {} days", retentionDays);
        // TODO: 遍历 EXPORT_DIR 删除过期文件
    }

    // ======== Private ========

    /**
     * 异步执行导出（@Async）
     */
    @Async
    void executeExport(String taskId) {
        log.info("Starting export execution for task {}", taskId);
        Optional<ExportTask> taskOpt = exportTaskRepository.findById(taskId);
        if (taskOpt.isEmpty()) return;

        ExportTask task = taskOpt.get();
        task.setStatus(ExportStatus.PROCESSING);
        exportTaskRepository.save(task);

        try {
            // 确保导出目录存在
            File dir = new File(EXPORT_DIR);
            if (!dir.exists()) dir.mkdirs();

            // 查询审计事件 — 使用简单全量查询
            AuditEventQuery legacyQuery = new AuditEventQuery();
            legacyQuery.setPage(1);
            legacyQuery.setSize(10000);
            AuditEventPage page = auditEventRepository.findAll(legacyQuery);
            List<AuditEvent> events = page.getItems();

            // 生成文件
            String extension = task.getFormat() == ExportFormat.EXCEL ? "xlsx" : "csv";
            String filename = "export-" + taskId + "-" +
                    LocalDateTime.now().toString().replace(":", "-") + "." + extension;
            String filePath = EXPORT_DIR + filename;

            switch (task.getFormat()) {
                case CSV -> generateCsv(events, filePath, task.isMaskEnabled());
                case EXCEL -> generateExcel(events, filePath, task.isMaskEnabled());
                case PDF -> throw new UnsupportedOperationException("PDF export coming in P7.1");
            }

            // 更新任务状态
            task.setFilePath(filePath);
            task.setStatus(ExportStatus.COMPLETED);
            task.setCompletedAt(LocalDateTime.now());
            exportTaskRepository.save(task);

            log.info("Export task {} completed: {} events → {}", taskId, events.size(), filePath);

        } catch (Exception e) {
            log.error("Export task {} failed: {}", taskId, e.getMessage(), e);
            task.setStatus(ExportStatus.FAILED);
            task.setErrorMessage(e.getMessage());
            exportTaskRepository.save(task);
        }
    }

    /**
     * 生成 CSV 文件（带签名列）
     */
    private void generateCsv(List<AuditEvent> events, String filePath, boolean mask) throws IOException {
        try (OutputStream os = new FileOutputStream(filePath)) {
            // BOM 头（Excel 兼容）
            os.write(0xEF);
            os.write(0xBB);
            os.write(0xBF);

            // CSV 头部（含签名列）
            String header = "id,module,action,eventType,targetType,targetId,operatorId,operatorName," +
                    "result,description,clientIp,createdAt,hash,previousHash,algorithm\n";
            os.write(header.getBytes(StandardCharsets.UTF_8));

            // 逐行写入
            for (AuditEvent event : events) {
                AuditEventResponse resp = AuditEventResponse.from(event);
                if (mask) {
                    maskService.applyMask(resp);
                }

                String hash = "";
                String previousHash = "";
                String algorithm = "";
                var sigOpt = signatureRepository.findByAuditId(event.getId());
                if (sigOpt.isPresent()) {
                    hash = sigOpt.get().getHash();
                    previousHash = sigOpt.get().getPreviousHash();
                    algorithm = sigOpt.get().getAlgorithm();
                }

                String row = escapeCsv(resp.getId()) + "," +
                        escapeCsv(resp.getModule()) + "," +
                        escapeCsv(resp.getAction()) + "," +
                        escapeCsv(resp.getEventType()) + "," +
                        escapeCsv(resp.getTargetType()) + "," +
                        escapeCsv(resp.getTargetId()) + "," +
                        escapeCsv(resp.getOperatorId()) + "," +
                        escapeCsv(resp.getOperatorName()) + "," +
                        escapeCsv(resp.getResult()) + "," +
                        escapeCsv(resp.getDescription()) + "," +
                        escapeCsv(resp.getClientIp()) + "," +
                        escapeCsv(resp.getCreatedAt() != null ? resp.getCreatedAt().toString() : "") + "," +
                        escapeCsv(hash) + "," +
                        escapeCsv(previousHash) + "," +
                        escapeCsv(algorithm) + "\n";
                os.write(row.getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    /**
     * 生成 Excel 文件
     */
    private void generateExcel(List<AuditEvent> events, String filePath, boolean mask) {
        // Excel 需要 Apache POI 依赖，暂时委托给 CSV
        throw new UnsupportedOperationException(
                "Excel export requires Apache POI dependency. Use CSV format instead.");
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String serializeQuery(AuditQuery query) {
        try {
            return objectMapper.writeValueAsString(query);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private AuditQuery deserializeQuery(String json) {
        // AuditQuery uses private constructor + Builder — use a simple approach
        return AuditQuery.builder().build();
    }

    /**
     * 将 AuditQuery DSL 转换为旧的 AuditEventQuery（兼容现有查询）
     */
    private AuditEventQuery convertToLegacyQuery(AuditQuery query) {
        AuditEventQuery legacy = new AuditEventQuery();
        legacy.setPage(query.getPage());
        legacy.setSize(query.getSize());
        if (query.getModule() != null) legacy.setModule(query.getModule());
        if (query.getAction() != null) legacy.setAction(query.getAction());
        if (query.getOperator() != null) legacy.setOperator(query.getOperator());
        if (query.getResult() != null) legacy.setResult(query.getResult());
        if (query.getKeyword() != null) legacy.setKeyword(query.getKeyword());
        if (query.getStartTime() != null) legacy.setStartTime(query.getStartTime());
        if (query.getEndTime() != null) legacy.setEndTime(query.getEndTime());
        if (query.getTraceId() != null) legacy.setTraceId(query.getTraceId());
        return legacy;
    }
}