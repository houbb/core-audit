package com.github.houbb.core.audit.infrastructure.persistence.repository;

import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.AuditEventPage;
import com.github.houbb.core.audit.application.domain.enums.AuditAction;
import com.github.houbb.core.audit.application.domain.enums.AuditModule;
import com.github.houbb.core.audit.application.domain.enums.AuditResult;
import com.github.houbb.core.audit.application.query.AuditEventQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@Import(JdbcAuditEventRepository.class)
@ActiveProfiles("test")
class JdbcAuditEventRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JdbcAuditEventRepository repository;

    @BeforeEach
    void setUp() {
        // 删除旧表（如果存在），然后创建含 P1 字段的测试表
        jdbcTemplate.execute("DROP TABLE IF EXISTS audit_event");
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS audit_event (
                    id              VARCHAR(255) PRIMARY KEY,
                    module          VARCHAR(50)  NOT NULL,
                    action          VARCHAR(50)  NOT NULL,
                    target_type     VARCHAR(100),
                    target_id       VARCHAR(255),
                    operator_id     VARCHAR(255),
                    operator_name   VARCHAR(255),
                    result          VARCHAR(20)  NOT NULL,
                    description     VARCHAR(500),
                    client_ip       VARCHAR(50),
                    request_uri     VARCHAR(500),
                    request_method  VARCHAR(10),
                    trace_id        VARCHAR(255),
                    metadata        VARCHAR(4000),
                    created_at      VARCHAR(30),
                    event_id         VARCHAR(255),
                    event_type       VARCHAR(100),
                    source           VARCHAR(100),
                    version          VARCHAR(20) DEFAULT '1.0',
                    occurred_at      VARCHAR(30),
                    published        INTEGER DEFAULT 1,
                    publish_time     VARCHAR(30),
                    publish_result   VARCHAR(20),
                    context_json     TEXT,
                    tenant           VARCHAR(50) DEFAULT 'default',
                    create_time     VARCHAR(30),
                    update_time     VARCHAR(30),
                    create_user     VARCHAR(100),
                    update_user     VARCHAR(100)
                )
                """);
    }

    @Test
    @DisplayName("保存审计事件并可通过 ID 查回")
    void shouldSaveAndRetrieveAuditEvent() {
        AuditEvent event = AuditEvent.builder()
                .module(AuditModule.USER)
                .action(AuditAction.DELETE)
                .targetType("USER")
                .targetId("1001")
                .operatorId("echo")
                .operatorName("echo")
                .result(AuditResult.SUCCESS)
                .description("删除用户 echo")
                .build();

        AuditEvent saved = repository.save(event);

        assertNotNull(saved.getId());
        assertEquals(AuditModule.USER, saved.getModule());
        assertEquals(AuditAction.DELETE, saved.getAction());

        Optional<AuditEvent> found = repository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("删除用户 echo", found.get().getDescription());
    }

    @Test
    @DisplayName("根据 ID 查找不存在的审计事件返回 empty")
    void shouldReturnEmptyForNonExistentId() {
        Optional<AuditEvent> found = repository.findById("non-existent-id");
        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("分页查询支持 module 过滤")
    void shouldFilterByModule() {
        // 插入 USER 事件
        repository.save(AuditEvent.builder()
                .module(AuditModule.USER).action(AuditAction.CREATE)
                .result(AuditResult.SUCCESS).description("创建用户").build());
        // 插入 AI 事件
        repository.save(AuditEvent.builder()
                .module(AuditModule.AI).action(AuditAction.CALL)
                .result(AuditResult.SUCCESS).description("调用 AI").build());

        AuditEventQuery query = new AuditEventQuery();
        query.setModule(AuditModule.USER);
        AuditEventPage page = repository.findAll(query);

        assertEquals(1, page.getItems().size());
        assertEquals(AuditModule.USER, page.getItems().get(0).getModule());
        assertEquals(1, page.getTotal());
    }

    @Test
    @DisplayName("分页查询支持 action 过滤")
    void shouldFilterByAction() {
        repository.save(AuditEvent.builder()
                .module(AuditModule.USER).action(AuditAction.CREATE)
                .result(AuditResult.SUCCESS).description("创建").build());
        repository.save(AuditEvent.builder()
                .module(AuditModule.USER).action(AuditAction.DELETE)
                .result(AuditResult.SUCCESS).description("删除").build());

        AuditEventQuery query = new AuditEventQuery();
        query.setAction(AuditAction.DELETE);
        AuditEventPage page = repository.findAll(query);

        assertEquals(1, page.getItems().size());
        assertEquals(AuditAction.DELETE, page.getItems().get(0).getAction());
    }

    @Test
    @DisplayName("分页查询支持 result 过滤")
    void shouldFilterByResult() {
        repository.save(AuditEvent.builder()
                .module(AuditModule.USER).action(AuditAction.CREATE)
                .result(AuditResult.SUCCESS).description("成功").build());
        repository.save(AuditEvent.builder()
                .module(AuditModule.USER).action(AuditAction.DELETE)
                .result(AuditResult.FAIL).description("失败").build());

        AuditEventQuery query = new AuditEventQuery();
        query.setResult(AuditResult.FAIL);
        AuditEventPage page = repository.findAll(query);

        assertEquals(1, page.getItems().size());
        assertEquals(AuditResult.FAIL, page.getItems().get(0).getResult());
    }

    @Test
    @DisplayName("分页查询支持关键字搜索")
    void shouldSearchByKeyword() {
        repository.save(AuditEvent.builder()
                .module(AuditModule.USER).action(AuditAction.CREATE)
                .operatorName("张三")
                .result(AuditResult.SUCCESS).description("创建用户 zhangsan").build());
        repository.save(AuditEvent.builder()
                .module(AuditModule.USER).action(AuditAction.CREATE)
                .operatorName("李四")
                .result(AuditResult.SUCCESS).description("创建用户 lisi").build());

        AuditEventQuery query = new AuditEventQuery();
        query.setKeyword("zhangsan");
        AuditEventPage page = repository.findAll(query);

        assertEquals(1, page.getItems().size());
        assertTrue(page.getItems().get(0).getDescription().contains("zhangsan"));
    }

    @Test
    @DisplayName("分页正确计算 total 和 hasNext")
    void shouldCalculatePaginationCorrectly() {
        for (int i = 0; i < 15; i++) {
            repository.save(AuditEvent.builder()
                    .module(AuditModule.USER).action(AuditAction.CREATE)
                    .result(AuditResult.SUCCESS).description("event-" + i).build());
        }

        AuditEventQuery query = new AuditEventQuery();
        query.setPage(1);
        query.setSize(10);
        AuditEventPage page1 = repository.findAll(query);

        assertEquals(10, page1.getItems().size());
        assertEquals(15, page1.getTotal());
        assertTrue(page1.isHasNext());

        query.setPage(2);
        AuditEventPage page2 = repository.findAll(query);

        assertEquals(5, page2.getItems().size());
        assertEquals(15, page2.getTotal());
        assertFalse(page2.isHasNext());
    }

    @Test
    @DisplayName("统计今天审计总数")
    void shouldCountToday() {
        repository.save(AuditEvent.builder()
                .module(AuditModule.USER).action(AuditAction.LOGIN)
                .result(AuditResult.SUCCESS).description("登录").build());
        repository.save(AuditEvent.builder()
                .module(AuditModule.USER).action(AuditAction.LOGOUT)
                .result(AuditResult.SUCCESS).description("登出").build());

        long count = repository.countToday();
        assertEquals(2, count);
    }

    @Test
    @DisplayName("统计今天成功和失败数")
    void shouldCountTodaySuccessAndFail() {
        repository.save(AuditEvent.builder()
                .module(AuditModule.USER).action(AuditAction.LOGIN)
                .result(AuditResult.SUCCESS).description("登录成功").build());
        repository.save(AuditEvent.builder()
                .module(AuditModule.USER).action(AuditAction.LOGIN)
                .result(AuditResult.FAIL).description("登录失败").build());
        repository.save(AuditEvent.builder()
                .module(AuditModule.USER).action(AuditAction.CREATE)
                .result(AuditResult.SUCCESS).description("创建").build());

        assertEquals(2, repository.countTodaySuccess());
        assertEquals(1, repository.countTodayFail());
    }

    @Test
    @DisplayName("统计活跃模块数")
    void shouldCountActiveModules() {
        repository.save(AuditEvent.builder()
                .module(AuditModule.USER).action(AuditAction.CREATE)
                .result(AuditResult.SUCCESS).build());
        repository.save(AuditEvent.builder()
                .module(AuditModule.AI).action(AuditAction.CALL)
                .result(AuditResult.SUCCESS).build());
        repository.save(AuditEvent.builder()
                .module(AuditModule.USER).action(AuditAction.DELETE)
                .result(AuditResult.SUCCESS).build());

        int count = repository.countActiveModulesToday();
        assertEquals(2, count);
    }
}