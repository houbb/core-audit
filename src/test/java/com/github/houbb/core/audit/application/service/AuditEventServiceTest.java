package com.github.houbb.core.audit.application.service;

import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.AuditEventPage;
import com.github.houbb.core.audit.application.domain.enums.AuditAction;
import com.github.houbb.core.audit.application.domain.enums.AuditModule;
import com.github.houbb.core.audit.application.domain.enums.AuditResult;
import com.github.houbb.core.audit.application.port.AuditEventRepository;
import com.github.houbb.core.audit.application.query.AuditEventQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditEventServiceTest {

    @Mock
    private AuditEventRepository repository;

    private AuditEventService service;

    @BeforeEach
    void setUp() {
        service = new AuditEventService(repository);
    }

    @Test
    @DisplayName("record 自动生成 ID 和创建时间")
    void shouldAutoGenerateIdAndTime() {
        AuditEvent event = AuditEvent.builder()
                .module(AuditModule.USER)
                .action(AuditAction.DELETE)
                .result(AuditResult.SUCCESS)
                .description("测试")
                .build();

        when(repository.save(any())).thenReturn(event);

        AuditEvent saved = service.record(event);

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        verify(repository).save(any());
    }

    @Test
    @DisplayName("record 保留已有的 ID")
    void shouldKeepExistingId() {
        AuditEvent event = AuditEvent.builder()
                .id("custom-id-123")
                .module(AuditModule.AI)
                .action(AuditAction.CALL)
                .result(AuditResult.SUCCESS)
                .build();

        when(repository.save(any())).thenReturn(event);

        AuditEvent saved = service.record(event);
        assertEquals("custom-id-123", saved.getId());
    }

    @Test
    @DisplayName("getById 转发到 repository")
    void shouldDelegateGetById() {
        AuditEvent event = AuditEvent.builder()
                .id("evt-1").module(AuditModule.USER)
                .action(AuditAction.CREATE).result(AuditResult.SUCCESS).build();
        when(repository.findById("evt-1")).thenReturn(Optional.of(event));

        Optional<AuditEvent> found = service.getById("evt-1");
        assertTrue(found.isPresent());
        assertEquals("evt-1", found.get().getId());
    }

    @Test
    @DisplayName("query 转发到 repository")
    void shouldDelegateQuery() {
        AuditEventPage page = new AuditEventPage(List.of(), 1, 20, 0);
        when(repository.findAll(any())).thenReturn(page);

        AuditEventQuery query = new AuditEventQuery();
        query.setModule(AuditModule.USER);
        AuditEventPage result = service.query(query);

        assertEquals(0, result.getTotal());
        verify(repository).findAll(query);
    }

    @Test
    @DisplayName("exportCsv 写入 CSV 内容")
    void shouldExportCsv() throws Exception {
        AuditEvent event = new AuditEvent();
        event.setId("evt-1");
        event.setModule(AuditModule.USER);
        event.setAction(AuditAction.DELETE);
        event.setTargetType("USER");
        event.setTargetId("1001");
        event.setOperatorName("echo");
        event.setResult(AuditResult.SUCCESS);
        event.setDescription("删除用户");

        AuditEventPage page = new AuditEventPage(List.of(event), 1, 20, 1);
        when(repository.findAll(any())).thenReturn(page);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        service.exportCsv(new AuditEventQuery(), baos);

        String csv = baos.toString("UTF-8");
        assertTrue(csv.contains("ID"));
        assertTrue(csv.contains("evt-1"));
        assertTrue(csv.contains("删除用户"));
    }

    @Test
    @DisplayName("getDashboardStats 汇总统计数据")
    void shouldReturnDashboardStats() {
        when(repository.countToday()).thenReturn(100L);
        when(repository.countTodaySuccess()).thenReturn(95L);
        when(repository.countTodayFail()).thenReturn(5L);
        when(repository.countActiveModulesToday()).thenReturn(3);
        when(repository.findAll(any())).thenReturn(new AuditEventPage(List.of(), 1, 10, 0));

        AuditEventService.DashboardStats stats = service.getDashboardStats();

        assertEquals(100, stats.getTodayTotal());
        assertEquals(95, stats.getTodaySuccess());
        assertEquals(5, stats.getTodayFail());
        assertEquals(3, stats.getActiveModules());
    }
}