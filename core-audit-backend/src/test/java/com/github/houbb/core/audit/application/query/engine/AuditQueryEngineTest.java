package com.github.houbb.core.audit.application.query.engine;

import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.diff.Change;
import com.github.houbb.core.audit.application.domain.enums.ChangeType;
import com.github.houbb.core.audit.application.domain.query.AuditQuery;
import com.github.houbb.core.audit.application.domain.query.AuditQueryResult;
import com.github.houbb.core.audit.application.port.ChangeRepository;
import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditEventEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditQueryEngine 单元测试")
class AuditQueryEngineTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private ChangeRepository changeRepository;

    private AuditQueryEngine engine;
    private QueryPlanner planner;

    @BeforeEach
    void setUp() {
        planner = new DefaultQueryPlanner(Collections.emptyList());
        engine = new AuditQueryEngine(jdbcTemplate, planner, changeRepository);
    }

    @Test
    @DisplayName("空查询返回空结果")
    void shouldReturnEmptyResultForEmptyQuery() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), any(Object[].class)))
                .thenReturn(0L);
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(Object[].class)))
                .thenReturn(Collections.emptyList());

        AuditQuery query = AuditQuery.builder().page(1).size(20).build();
        AuditQueryResult result = engine.execute(query);

        assertNotNull(result);
        assertEquals(0, result.getTotal());
        assertTrue(result.getItems().isEmpty());
        assertFalse(result.isHasNext());
        assertTrue(result.getDiffChanges().isEmpty());
    }

    @Test
    @DisplayName("有结果时正确返回 AuditEvent 列表")
    void shouldReturnAuditEventsWhenFound() {
        AuditEventEntity entity = buildEntity("USER", "CREATE", "SUCCESS", "testuser", "创建用户");

        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), any(Object[].class)))
                .thenReturn(1L);
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(Object[].class)))
                .thenReturn(List.of(entity));

        AuditQuery query = AuditQuery.builder().page(1).size(20).build();
        AuditQueryResult result = engine.execute(query);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getItems().size());
        AuditEvent event = result.getItems().get(0);
        assertEquals("testuser", event.getOperatorName());
        assertEquals("创建用户", event.getDescription());
        assertTrue(result.getDiffChanges().isEmpty());
    }

    @Test
    @DisplayName("分页信息正确计算")
    void shouldCalculatePaginationCorrectly() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), any(Object[].class)))
                .thenReturn(100L);
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(Object[].class)))
                .thenReturn(Collections.emptyList());

        AuditQuery query = AuditQuery.builder().page(2).size(20).build();
        AuditQueryResult result = engine.execute(query);

        assertEquals(2, result.getPage());
        assertEquals(20, result.getSize());
        assertEquals(100, result.getTotal());
        assertTrue(result.isHasNext());
    }

    @Test
    @DisplayName("最后一页 hasNext 为 false")
    void shouldHaveNoNextOnLastPage() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), any(Object[].class)))
                .thenReturn(100L);
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(Object[].class)))
                .thenReturn(Collections.emptyList());

        AuditQuery query = AuditQuery.builder().page(5).size(20).build();
        AuditQueryResult result = engine.execute(query);

        assertFalse(result.isHasNext());
    }

    @Test
    @DisplayName("Diff JOIN 查询附带变更数据")
    void shouldAttachDiffChangesWhenDiffJoin() {
        AuditEventEntity entity = buildEntity("USER", "UPDATE", "SUCCESS", "testuser", "更新用户角色");

        Change change = Change.builder()
                .fieldName("role")
                .changeType(ChangeType.UPDATE)
                .beforeValue("USER")
                .afterValue("ADMIN")
                .build();

        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), any(Object[].class)))
                .thenReturn(1L);
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(Object[].class)))
                .thenReturn(List.of(entity));
        when(changeRepository.findByAuditId(entity.getId())).thenReturn(List.of(change));

        AuditQuery query = AuditQuery.builder()
                .diffField("role")
                .page(1).size(20)
                .build();

        AuditQueryResult result = engine.execute(query);

        assertNotNull(result);
        assertFalse(result.getDiffChanges().isEmpty());
        assertTrue(result.getDiffChanges().containsKey(entity.getId()));
        assertEquals(1, result.getDiffChanges().get(entity.getId()).size());
        assertEquals("role", result.getDiffChanges().get(entity.getId()).get(0).getFieldName());
    }

    private AuditEventEntity buildEntity(String module, String action, String result,
                                          String operatorName, String description) {
        AuditEventEntity entity = new AuditEventEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setModule(module);
        entity.setAction(action);
        entity.setResult(result);
        entity.setOperatorName(operatorName);
        entity.setDescription(description);
        entity.setCreatedAt(LocalDateTime.now().toString());
        entity.setCreateTime(LocalDateTime.now().toString());
        entity.setUpdateTime(LocalDateTime.now().toString());
        entity.setCreateUser("system");
        entity.setUpdateUser("system");
        entity.setPublished(1);
        return entity;
    }
}