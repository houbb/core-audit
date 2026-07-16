package com.github.houbb.core.audit.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.houbb.core.audit.api.request.CreateAuditEventRequest;
import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.enums.AuditAction;
import com.github.houbb.core.audit.application.domain.enums.AuditModule;
import com.github.houbb.core.audit.application.domain.enums.AuditResult;
import com.github.houbb.core.audit.application.service.AuditEventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller 层单元测试（不启动 Spring 上下文，纯 MockMvc）
 */
@ExtendWith(MockitoExtension.class)
class AuditEventControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuditEventService auditEventService;

    @InjectMocks
    private AuditEventController auditEventController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                        auditEventController,
                        new AuditDashboardController(auditEventService))
                .setControllerAdvice(new com.github.houbb.core.audit.api.exception.GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/audit/events 创建审计事件返回 201")
    void shouldCreateAuditEvent() throws Exception {
        CreateAuditEventRequest req = new CreateAuditEventRequest();
        req.setModule(AuditModule.USER);
        req.setAction(AuditAction.DELETE);
        req.setTargetType("USER");
        req.setTargetId("1001");
        req.setOperatorName("echo");
        req.setResult(AuditResult.SUCCESS);
        req.setDescription("删除用户");

        AuditEvent saved = new AuditEvent();
        saved.setId("evt-test-1");
        saved.setModule(AuditModule.USER);
        saved.setAction(AuditAction.DELETE);
        saved.setTargetType("USER");
        saved.setTargetId("1001");
        saved.setOperatorName("echo");
        saved.setResult(AuditResult.SUCCESS);
        saved.setDescription("删除用户");
        saved.setCreatedAt(LocalDateTime.now());

        when(auditEventService.record(any())).thenReturn(saved);

        mockMvc.perform(post("/api/v1/audit/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("evt-test-1"))
                .andExpect(jsonPath("$.module").value("USER"))
                .andExpect(jsonPath("$.action").value("DELETE"));
    }

    @Test
    @DisplayName("POST /api/v1/audit/events 缺少必填字段返回 400")
    void shouldReturn400WhenMissingRequiredFields() throws Exception {
        String invalidJson = "{\"module\":null,\"action\":null}";

        mockMvc.perform(post("/api/v1/audit/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/audit/events 返回分页结果")
    void shouldReturnPaginatedEvents() throws Exception {
        when(auditEventService.query(any())).thenReturn(
                new com.github.houbb.core.audit.application.domain.AuditEventPage(
                        java.util.List.of(), 1, 20, 0));

        mockMvc.perform(get("/api/v1/audit/events")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/audit/events/{id} 不存在返回 404")
    void shouldReturn404ForNonExistentEvent() throws Exception {
        when(auditEventService.getById("non-existent")).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/api/v1/audit/events/non-existent"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/audit/dashboard 返回统计")
    void shouldReturnDashboard() throws Exception {
        when(auditEventService.getDashboardStats()).thenReturn(
                new AuditEventService.DashboardStats());

        mockMvc.perform(get("/api/v1/audit/dashboard"))
                .andExpect(status().isOk());
    }
}