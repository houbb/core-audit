package com.github.houbb.core.audit.application.query.service;

import com.github.houbb.core.audit.application.domain.enums.AuditAction;
import com.github.houbb.core.audit.application.domain.enums.AuditModule;
import com.github.houbb.core.audit.application.domain.query.AuditQuery;
import com.github.houbb.core.audit.application.port.SavedQueryRepository;
import com.github.houbb.core.audit.application.port.SavedQueryRepository.SavedQueryRecord;
import com.github.houbb.core.audit.application.port.SearchHistoryRepository;
import com.github.houbb.core.audit.application.port.SearchHistoryRepository.SearchHistoryRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("QueryHistoryService 单元测试")
class QueryHistoryServiceTest {

    @Mock
    private SearchHistoryRepository searchHistoryRepository;

    @Mock
    private SavedQueryRepository savedQueryRepository;

    private QueryHistoryService service;

    @BeforeEach
    void setUp() {
        service = new QueryHistoryService(searchHistoryRepository, savedQueryRepository);
    }

    @Test
    @DisplayName("记录搜索历史 — 正确序列化并保存")
    void shouldRecordSearchHistory() {
        AuditQuery query = AuditQuery.builder()
                .module(AuditModule.USER)
                .action(AuditAction.DELETE)
                .keyword("test")
                .build();

        service.recordSearch("user-1", query);

        ArgumentCaptor<String> userIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> timeCaptor = ArgumentCaptor.forClass(String.class);

        verify(searchHistoryRepository).save(userIdCaptor.capture(), jsonCaptor.capture(), timeCaptor.capture());

        assertEquals("user-1", userIdCaptor.getValue());
        assertNotNull(jsonCaptor.getValue());
        assertTrue(jsonCaptor.getValue().contains("USER"));
        assertTrue(jsonCaptor.getValue().contains("DELETE"));
        assertTrue(jsonCaptor.getValue().contains("test"));
        assertNotNull(timeCaptor.getValue());
    }

    @Test
    @DisplayName("获取最近搜索 — 最多返回 20 条")
    void shouldGetRecentSearches() {
        SearchHistoryRecord record = new SearchHistoryRecord(
                "id-1", "user-1", "{\"module\":\"USER\"}", "2026-07-17T10:00:00"
        );
        when(searchHistoryRepository.findRecentByUser("user-1", 20))
                .thenReturn(List.of(record));

        List<SearchHistoryRecord> results = service.getRecentSearches("user-1");

        assertEquals(1, results.size());
        assertEquals("user-1", results.get(0).getUserId());
    }

    @Test
    @DisplayName("保存查询 — 正确序列化并返回记录")
    void shouldSaveQuery() {
        AuditQuery query = AuditQuery.builder()
                .module(AuditModule.CONFIG)
                .action(AuditAction.UPDATE)
                .build();

        SavedQueryRecord expected = new SavedQueryRecord(
                "q-1", "生产配置变更", "user-1", "{\"module\":\"CONFIG\"}", false, "2026-07-17T10:00:00"
        );
        when(savedQueryRepository.save(eq("生产配置变更"), eq("user-1"), anyString(), eq(false)))
                .thenReturn(expected);

        SavedQueryRecord result = service.saveQuery("生产配置变更", "user-1", query, false);

        assertNotNull(result);
        assertEquals("生产配置变更", result.getName());
        assertEquals("q-1", result.getId());
    }

    @Test
    @DisplayName("删除保存查询 — 成功时返回 true")
    void shouldDeleteSavedQuery() {
        when(savedQueryRepository.deleteById("q-1")).thenReturn(true);

        boolean deleted = service.deleteSavedQuery("q-1");

        assertTrue(deleted);
        verify(savedQueryRepository).deleteById("q-1");
    }

    @Test
    @DisplayName("删除保存查询 — 不存在时返回 false")
    void shouldReturnFalseWhenDeleteNonexistent() {
        when(savedQueryRepository.deleteById("nonexistent")).thenReturn(false);

        boolean deleted = service.deleteSavedQuery("nonexistent");

        assertFalse(deleted);
    }

    @Test
    @DisplayName("获取已保存查询列表")
    void shouldGetSavedQueries() {
        SavedQueryRecord record = new SavedQueryRecord(
                "q-1", "我的查询", "user-1", "{\"keyword\":\"test\"}", false, "2026-07-17"
        );
        when(savedQueryRepository.findByOwner("user-1")).thenReturn(List.of(record));

        List<SavedQueryRecord> results = service.getSavedQueries("user-1");

        assertEquals(1, results.size());
        assertEquals("我的查询", results.get(0).getName());
    }
}