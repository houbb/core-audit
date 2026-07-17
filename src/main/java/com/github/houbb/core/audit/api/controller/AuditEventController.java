package com.github.houbb.core.audit.api.controller;

import com.github.houbb.core.audit.api.request.CreateAuditEventRequest;
import com.github.houbb.core.audit.api.response.AuditEventPageResponse;
import com.github.houbb.core.audit.api.response.AuditEventResponse;
import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.AuditEventPage;
import com.github.houbb.core.audit.application.query.AuditEventQuery;
import com.github.houbb.core.audit.application.service.AuditEventService;
import com.github.houbb.core.audit.application.domain.enums.AuditAction;
import com.github.houbb.core.audit.application.domain.enums.AuditEventType;
import com.github.houbb.core.audit.application.domain.enums.AuditModule;
import com.github.houbb.core.audit.application.domain.enums.AuditResult;
import com.github.houbb.core.audit.api.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 审计事件 REST API
 * <p>提供写入、查询、详情、导出四个核心端点。</p>
 */
@RestController
@RequestMapping("/api/v1/audit")
@Tag(name = "Audit Events", description = "审计事件管理 API")
public class AuditEventController {

    private final AuditEventService auditEventService;

    public AuditEventController(AuditEventService auditEventService) {
        this.auditEventService = auditEventService;
    }

    /**
     * 写入审计事件
     */
    @PostMapping("/events")
    @Operation(summary = "写入审计事件", description = "记录一条审计事件，一般由内部模块调用")
    public ResponseEntity<AuditEventResponse> createEvent(
            @Valid @RequestBody CreateAuditEventRequest request) {

        AuditEvent event = new AuditEvent();
        event.setId(request.getId());
        event.setModule(request.getModule());
        event.setAction(request.getAction());
        event.setTargetType(request.getTargetType());
        event.setTargetId(request.getTargetId());
        event.setOperatorId(request.getOperatorId());
        event.setOperatorName(request.getOperatorName());
        event.setResult(request.getResult());
        event.setDescription(request.getDescription());
        event.setClientIp(request.getClientIp());
        event.setRequestUri(request.getRequestUri());
        event.setRequestMethod(request.getRequestMethod());
        event.setTraceId(request.getTraceId());
        event.setCreatedAt(request.getCreatedAt());
        event.setMetadata(request.getMetadata());
        // P1 fields
        event.setEventType(request.getEventType());
        event.setSource(request.getSource());
        event.setVersion(request.getVersion());
        event.setPublish(request.getPublish());

        AuditEvent saved = auditEventService.record(event);
        return ResponseEntity.status(HttpStatus.CREATED).body(AuditEventResponse.from(saved));
    }

    /**
     * 分页查询审计事件
     */
    @GetMapping("/events")
    @Operation(summary = "查询审计事件", description = "支持分页、多条件过滤、关键字搜索")
    public ResponseEntity<AuditEventPageResponse> queryEvents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String operator,
            @RequestParam(required = false) String result,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String traceId,
            // ======== P2 context filter params ========
            @RequestParam(required = false) String tenant,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String browser,
            @RequestParam(required = false) String ip,
            @RequestParam(required = false) String workspace,
            @RequestParam(required = false) String project) {

        AuditEventQuery query = new AuditEventQuery();
        query.setPage(page);
        query.setSize(size);
        if (module != null && !module.isBlank()) query.setModule(AuditModule.valueOf(module));
        if (action != null && !action.isBlank()) query.setAction(AuditAction.valueOf(action));
        if (operator != null && !operator.isBlank()) query.setOperator(operator);
        if (result != null && !result.isBlank()) query.setResult(AuditResult.valueOf(result));
        if (keyword != null && !keyword.isBlank()) query.setKeyword(keyword);
        if (eventType != null && !eventType.isBlank()) query.setEventType(AuditEventType.valueOf(eventType));
        if (startTime != null && !startTime.isBlank()) query.setStartTime(LocalDateTime.parse(startTime));
        if (endTime != null && !endTime.isBlank()) query.setEndTime(LocalDateTime.parse(endTime));
        if (traceId != null && !traceId.isBlank()) query.setTraceId(traceId);
        if (tenant != null && !tenant.isBlank()) query.setTenant(tenant);
        if (department != null && !department.isBlank()) query.setDepartment(department);
        if (browser != null && !browser.isBlank()) query.setBrowser(browser);
        if (ip != null && !ip.isBlank()) query.setIp(ip);
        if (workspace != null && !workspace.isBlank()) query.setWorkspace(workspace);
        if (project != null && !project.isBlank()) query.setProject(project);

        AuditEventPage pageResult = auditEventService.query(query);

        AuditEventPageResponse response = new AuditEventPageResponse(
                pageResult.getItems().stream().map(AuditEventResponse::from).toList(),
                pageResult.getPage(),
                pageResult.getSize(),
                pageResult.getTotal(),
                pageResult.isHasNext()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 查看审计事件详情
     */
    @GetMapping("/events/{id}")
    @Operation(summary = "查看审计事件详情", description = "根据 ID 获取审计事件的完整信息，包含 metadata")
    public ResponseEntity<Object> getEvent(@PathVariable String id) {
        return auditEventService.getById(id)
                .<ResponseEntity<Object>>map(event -> ResponseEntity.ok(AuditEventResponse.from(event)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.notFound("No audit event found with id: " + id)));
    }

    /**
     * 导出审计事件为 CSV
     */
    @GetMapping("/events/export")
    @Operation(summary = "导出审计事件", description = "将符合条件的审计事件导出为 CSV 文件")
    public void exportEvents(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String operator,
            @RequestParam(required = false) String result,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String traceId,
            // ======== P2 context filter params ========
            @RequestParam(required = false) String tenant,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String browser,
            @RequestParam(required = false) String ip,
            @RequestParam(required = false) String workspace,
            @RequestParam(required = false) String project,
            HttpServletResponse response) throws IOException {

        AuditEventQuery query = new AuditEventQuery();
        if (module != null && !module.isBlank()) query.setModule(AuditModule.valueOf(module));
        if (action != null && !action.isBlank()) query.setAction(AuditAction.valueOf(action));
        if (operator != null && !operator.isBlank()) query.setOperator(operator);
        if (result != null && !result.isBlank()) query.setResult(AuditResult.valueOf(result));
        if (keyword != null && !keyword.isBlank()) query.setKeyword(keyword);
        if (startTime != null && !startTime.isBlank()) query.setStartTime(LocalDateTime.parse(startTime));
        if (endTime != null && !endTime.isBlank()) query.setEndTime(LocalDateTime.parse(endTime));
        if (traceId != null && !traceId.isBlank()) query.setTraceId(traceId);
        if (tenant != null && !tenant.isBlank()) query.setTenant(tenant);
        if (department != null && !department.isBlank()) query.setDepartment(department);
        if (browser != null && !browser.isBlank()) query.setBrowser(browser);
        if (ip != null && !ip.isBlank()) query.setIp(ip);
        if (workspace != null && !workspace.isBlank()) query.setWorkspace(workspace);
        if (project != null && !project.isBlank()) query.setProject(project);

        String filename = "audit-events-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv";
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        auditEventService.exportCsv(query, response.getOutputStream());
    }
}