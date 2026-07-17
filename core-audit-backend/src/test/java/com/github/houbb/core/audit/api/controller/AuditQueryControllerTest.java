package com.github.houbb.core.audit.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.diff.Change;
import com.github.houbb.core.audit.application.domain.enums.AuditAction;
import com.github.houbb.core.audit.application.domain.enums.AuditModule;
import com.github.houbb.core.audit.application.domain.enums.AuditResult;
import com.github.houbb.core.audit.application.domain.enums.ChangeType;
import com.github.houbb.core.audit.application.domain.query.AuditQuery;
import com.github.houbb.core.audit.application.domain.query.AuditQueryResult;
import com.github.houbb.core.audit.application.query.engine.AuditQueryEngine;
import com.github.houbb.core.audit.application.query.service.QueryHistoryService;
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
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditQueryController 单元测试")
class AuditQueryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuditQueryEngine auditQueryEngine;

    @Mock
    private QueryHistoryService queryHistoryService;

    @InjectMocks
    private AuditQueryController auditQueryController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(auditQueryController)
                .setControllerAdvice(new com.github.houbb.core.audit.api.exception.GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/audit/query — 空查询返回 200")
    void shouldReturn200ForEmptyQuery() throws Exception {
        AuditQueryResult mockResult = new AuditQueryResult(List.of(), 1, 20, 0);
        when(auditQueryEngine.execute(any(AuditQuery.class))).thenReturn(mockResult);
        doNothing().when(queryHistoryService).recordSearch(anyString(), any(AuditQuery.class));

        String body = """
                {
                    "page": 1,
                    "size": 20
                }
                """;

        mockMvc.perform(post("/api/v1/audit/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    @DisplayName("POST /api/v1/audit/query — 带过滤条件查询返回 200")
    void shouldReturn200ForFilteredQuery() throws Exception {
        AuditEvent event = new AuditEvent();
        event.setId("evt-001");
        event.setModule(AuditModule.USER);
        event.setAction(AuditAction.DELETE);
        event.setResult(AuditResult.SUCCESS);
        event.setOperatorName("echo");
        event.setCreatedAt(LocalDateTime.now());

        AuditQueryResult mockResult = new AuditQueryResult(List.of(event), 1, 20, 1);
        when(auditQueryEngine.execute(any(AuditQuery.class))).thenReturn(mockResult);
        doNothing().when(queryHistoryService).recordSearch(anyString(), any(AuditQuery.class));

        String body = """
                {
                    "module": "USER",
                    "action": "DELETE",
                    "page": 1,
                    "size": 20
                }
                """;

        mockMvc.perform(post("/api/v1/audit/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value("evt-001"))
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    @DisplayName("POST /api/v1/audit/query — Diff 查询附带变更数据")
    void shouldReturnDiffChangesForDiffQuery() throws Exception {
        AuditEvent event = new AuditEvent();
        event.setId("evt-002");
        event.setModule(AuditModule.CONFIG);
        event.setAction(AuditAction.UPDATE);
        event.setResult(AuditResult.SUCCESS);
        event.setOperatorName("admin");
        event.setCreatedAt(LocalDateTime.now());

        Change change = Change.builder()
                .fieldName("role")
                .changeType(ChangeType.UPDATE)
                .beforeValue("USER")
                .afterValue("ADMIN")
                .build();

        AuditQueryResult mockResult = new AuditQueryResult(
                List.of(event), 1, 20, 1,
                Map.of("evt-002", List.of(change))
        );
        when(auditQueryEngine.execute(any(AuditQuery.class))).thenReturn(mockResult);
        doNothing().when(queryHistoryService).recordSearch(anyString(), any(AuditQuery.class));

        String body = """
                {
                    "diffField": "role",
                    "page": 1,
                    "size": 20
                }
                """;

        mockMvc.perform(post("/api/v1/audit/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diffChanges.evt-002").isArray())
                .andExpect(jsonPath("$.diffChanges.evt-002[0].field").value("role"));
    }

    @Test
    @DisplayName("POST /api/v1/audit/query — page 非法值时返回 400")
    void shouldReturn400ForInvalidPage() throws Exception {
        String body = """
                {
                    "page": 0,
                    "size": 20
                }
                """;

        mockMvc.perform(post("/api/v1/audit/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/audit/query — size 超过 100 时返回 400")
    void shouldReturn400ForInvalidSize() throws Exception {
        String body = """
                {
                    "page": 1,
                    "size": 200
                }
                """;

        mockMvc.perform(post("/api/v1/audit/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/audit/query/recent — 返回最近搜索历史")
    void shouldReturnRecentSearches() throws Exception {
        var record = new com.github.houbb.core.audit.application.port.SearchHistoryRepository.SearchHistoryRecord(
                "h-1", "user-1", "{\"module\":\"USER\"}", "2026-07-17T10:00:00"
        );
        when(queryHistoryService.getRecentSearches("user-1")).thenReturn(List.of(record));

        mockMvc.perform(get("/api/v1/audit/query/recent")
                        .param("userId", "user-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("h-1"))
                .andExpect(jsonPath("$[0].userId").value("user-1"));
    }

    @Test
    @DisplayName("GET /api/v1/audit/query/saved — 返回已保存查询")
    void shouldReturnSavedQueries() throws Exception {
        var record = new com.github.houbb.core.audit.application.port.SavedQueryRepository.SavedQueryRecord(
                "q-1", "生产配置变更", "user-1", "{\"module\":\"CONFIG\"}", false, "2026-07-17"
        );
        when(queryHistoryService.getSavedQueries("user-1")).thenReturn(List.of(record));

        mockMvc.perform(get("/api/v1/audit/query/saved")
                        .param("userId", "user-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("q-1"))
                .andExpect(jsonPath("$[0].name").value("生产配置变更"));
    }

    @Test
    @DisplayName("DELETE /api/v1/audit/query/save/{id} — 删除成功返回 204")
    void shouldReturn204WhenDeleteSuccess() throws Exception {
        when(queryHistoryService.deleteSavedQuery("q-1")).thenReturn(true);

        mockMvc.perform(delete("/api/v1/audit/query/save/q-1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/audit/query/save/{id} — 删除不存在返回 404")
    void shouldReturn404WhenDeleteNonexistent() throws Exception {
        when(queryHistoryService.deleteSavedQuery("nonexistent")).thenReturn(false);

        mockMvc.perform(delete("/api/v1/audit/query/save/nonexistent"))
                .andExpect(status().isNotFound());
    }
}