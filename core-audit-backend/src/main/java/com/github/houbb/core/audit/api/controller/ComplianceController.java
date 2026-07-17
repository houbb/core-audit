package com.github.houbb.core.audit.api.controller;

import com.github.houbb.core.audit.api.request.CreateLegalHoldRequest;
import com.github.houbb.core.audit.api.request.ExportRequest;
import com.github.houbb.core.audit.api.request.RetentionPolicyRequest;
import com.github.houbb.core.audit.api.response.*;
import com.github.houbb.core.audit.application.domain.compliance.*;
import com.github.houbb.core.audit.application.domain.enums.AuditAction;
import com.github.houbb.core.audit.application.domain.enums.AuditModule;
import com.github.houbb.core.audit.application.domain.enums.ExportFormat;
import com.github.houbb.core.audit.application.domain.enums.ExportStatus;
import com.github.houbb.core.audit.application.domain.query.AuditQuery;
import com.github.houbb.core.audit.application.port.AuditSignatureRepository;
import com.github.houbb.core.audit.application.port.ExportTaskRepository;
import com.github.houbb.core.audit.application.service.ComplianceService;
import com.github.houbb.core.audit.application.service.ExportService;
import com.github.houbb.core.audit.application.service.IntegrityService;
import com.github.houbb.core.audit.application.service.IntegrityService.ChainVerificationResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Compliance API — P7 Compliance Runtime
 * <p>提供 retention、integrity、legal hold、export 四大合规能力的 REST 接口。</p>
 */
@RestController
@RequestMapping("/api/v1/audit")
@Tag(name = "Compliance", description = "合规管理 API")
public class ComplianceController {

    private final ComplianceService complianceService;
    private final ExportService exportService;
    private final IntegrityService integrityService;
    private final AuditSignatureRepository signatureRepository;
    private final ExportTaskRepository exportTaskRepository;

    public ComplianceController(ComplianceService complianceService,
                                ExportService exportService,
                                IntegrityService integrityService,
                                AuditSignatureRepository signatureRepository,
                                ExportTaskRepository exportTaskRepository) {
        this.complianceService = complianceService;
        this.exportService = exportService;
        this.integrityService = integrityService;
        this.signatureRepository = signatureRepository;
        this.exportTaskRepository = exportTaskRepository;
    }

    // ======== Overview ========

    @GetMapping("/compliance/overview")
    @Operation(summary = "合规概览", description = "返回合规 Dashboard 统计数据")
    public ResponseEntity<ComplianceOverviewResponse> getOverview() {
        ComplianceService.ComplianceOverview overview = complianceService.getOverview();
        ComplianceOverviewResponse resp = new ComplianceOverviewResponse(
                overview.getRetentionPolicyCount(),
                overview.getHashVerificationRate(),
                overview.getLegalHoldCount(),
                overview.getTodayExportCount()
        );
        return ResponseEntity.ok(resp);
    }

    // ======== Retention Policies ========

    @GetMapping("/compliance/policies")
    @Operation(summary = "列出所有保留策略")
    public ResponseEntity<List<RetentionPolicyResponse>> listPolicies() {
        List<RetentionPolicyResponse> list = complianceService.listPolicies().stream()
                .map(RetentionPolicyResponse::from).toList();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/compliance/policies")
    @Operation(summary = "创建保留策略")
    public ResponseEntity<RetentionPolicyResponse> createPolicy(@Valid @RequestBody RetentionPolicyRequest request) {
        RetentionPolicy policy = RetentionPolicy.builder()
                .module(AuditModule.valueOf(request.getModule()))
                .action(request.getAction() != null ? AuditAction.valueOf(request.getAction()) : null)
                .retentionDays(request.getRetentionDays())
                .archive(request.isArchive())
                .enabled(request.isEnabled())
                .build();
        RetentionPolicy saved = complianceService.savePolicy(policy);
        return ResponseEntity.ok(RetentionPolicyResponse.from(saved));
    }

    @DeleteMapping("/compliance/policies/{id}")
    @Operation(summary = "删除保留策略")
    public ResponseEntity<Void> deletePolicy(@PathVariable String id) {
        complianceService.deletePolicy(id);
        return ResponseEntity.noContent().build();
    }

    // ======== Integrity ========

    @GetMapping("/compliance/hash/{auditId}")
    @Operation(summary = "获取审计签名及验证状态", description = "返回指定审计事件的 SHA-256 签名及其验证结果")
    public ResponseEntity<IntegrityResponse> getSignature(@PathVariable String auditId) {
        Optional<AuditSignature> sigOpt = signatureRepository.findByAuditId(auditId);
        if (sigOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        boolean verified = integrityService.verify(auditId);
        return ResponseEntity.ok(IntegrityResponse.from(sigOpt.get(), verified));
    }

    @PostMapping("/compliance/verify")
    @Operation(summary = "验证哈希链完整性", description = "对整个哈希链进行完整性校验")
    public ResponseEntity<ChainVerificationResult> verifyChain(
            @RequestParam(defaultValue = "500") int batchSize) {
        ChainVerificationResult result = integrityService.verifyChain(batchSize);
        return ResponseEntity.ok(result);
    }

    // ======== Legal Hold ========

    @GetMapping("/compliance/legal-holds")
    @Operation(summary = "列出所有法律保留")
    public ResponseEntity<List<LegalHoldResponse>> listLegalHolds() {
        List<LegalHoldResponse> list = complianceService.listLegalHolds().stream()
                .map(LegalHoldResponse::from).toList();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/compliance/legal-hold")
    @Operation(summary = "创建法律保留")
    public ResponseEntity<LegalHoldResponse> createLegalHold(@Valid @RequestBody CreateLegalHoldRequest request) {
        LegalHold hold = LegalHold.builder()
                .auditId(request.getAuditId())
                .reason(request.getReason())
                .owner(request.getOwner())
                .expiredAt(request.getExpiredAt() != null ? LocalDateTime.parse(request.getExpiredAt()) : null)
                .build();
        LegalHold saved = complianceService.createLegalHold(hold);
        return ResponseEntity.ok(LegalHoldResponse.from(saved));
    }

    @DeleteMapping("/compliance/legal-hold/{id}")
    @Operation(summary = "释放法律保留")
    public ResponseEntity<Void> releaseLegalHold(@PathVariable String id) {
        complianceService.releaseLegalHold(id);
        return ResponseEntity.noContent().build();
    }

    // ======== Export ========

    @PostMapping("/compliance/export")
    @Operation(summary = "提交导出任务", description = "异步导出审计数据，返回任务 ID 供轮询")
    public ResponseEntity<ExportTaskResponse> submitExport(@Valid @RequestBody ExportRequest request) {
        AuditQuery query = AuditQuery.builder()
                .page(1)
                .size(10000)
                .build();
        if (request.getModule() != null) query.setModule(AuditModule.valueOf(request.getModule()));
        if (request.getAction() != null) query.setAction(AuditAction.valueOf(request.getAction()));
        if (request.getStartTime() != null) query.setStartTime(LocalDateTime.parse(request.getStartTime()));
        if (request.getEndTime() != null) query.setEndTime(LocalDateTime.parse(request.getEndTime()));
        if (request.getKeyword() != null) query.setKeyword(request.getKeyword());

        ExportTask task = exportService.submitExport(
                query,
                request.getFormat() != null ? request.getFormat() : ExportFormat.CSV,
                request.isMaskEnabled(),
                request.isIncludeDiff(),
                request.isIncludeTimeline(),
                "system");
        return ResponseEntity.ok(ExportTaskResponse.from(task));
    }

    @GetMapping("/compliance/export/{taskId}")
    @Operation(summary = "查询导出任务状态")
    public ResponseEntity<ExportTaskResponse> getExportStatus(@PathVariable String taskId) {
        Optional<ExportTask> taskOpt = exportService.getTask(taskId);
        return taskOpt.map(task -> ResponseEntity.ok(ExportTaskResponse.from(task)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/compliance/export/{taskId}/download")
    @Operation(summary = "下载导出文件")
    public void downloadExport(@PathVariable String taskId, HttpServletResponse response) throws IOException {
        Optional<ExportTask> taskOpt = exportTaskRepository.findById(taskId);
        if (taskOpt.isEmpty() || taskOpt.get().getStatus() != ExportStatus.COMPLETED || taskOpt.get().getFilePath() == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Export file not found or not ready");
            return;
        }

        ExportTask task = taskOpt.get();
        byte[] data = exportService.downloadExport(taskId);
        if (data == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Export file not found");
            return;
        }

        String filename = "export-" + taskId + "." +
                (task.getFormat() == ExportFormat.EXCEL ? "xlsx" : "csv");
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + filename + "\"");
        response.getOutputStream().write(data);
    }

    @GetMapping("/compliance/export/history")
    @Operation(summary = "导出历史")
    public ResponseEntity<List<ExportTaskResponse>> getExportHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<ExportTaskResponse> list = exportService.getHistory(page, size).stream()
                .map(ExportTaskResponse::from).toList();
        return ResponseEntity.ok(list);
    }
}