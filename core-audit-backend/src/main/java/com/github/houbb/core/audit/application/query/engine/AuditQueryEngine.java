package com.github.houbb.core.audit.application.query.engine;

import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.diff.Change;
import com.github.houbb.core.audit.application.domain.query.AuditQuery;
import com.github.houbb.core.audit.application.domain.query.AuditQueryResult;
import com.github.houbb.core.audit.application.port.ChangeRepository;
import com.github.houbb.core.audit.infrastructure.persistence.converter.AuditEventConverter;
import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditEventEntity;
import com.github.houbb.core.audit.infrastructure.persistence.jdbc.AuditEventRowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 统一审计查询引擎
 * <p>P4 Search Runtime 的核心入口。接收 AuditQuery DSL，
 * 通过 QueryPlanner 生成查询计划，执行查询并返回结果。</p>
 */
@Service
public class AuditQueryEngine {

    private static final Logger log = LoggerFactory.getLogger(AuditQueryEngine.class);

    private final JdbcTemplate jdbcTemplate;
    private final QueryPlanner queryPlanner;
    private final ChangeRepository changeRepository;
    private final AuditEventRowMapper rowMapper = new AuditEventRowMapper();

    public AuditQueryEngine(JdbcTemplate jdbcTemplate, QueryPlanner queryPlanner,
                            ChangeRepository changeRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.queryPlanner = queryPlanner;
        this.changeRepository = changeRepository;
    }

    /**
     * 执行审计查询
     *
     * @param query 统一查询 DSL
     * @return 查询结果（含分页 + Diff 变更数据）
     */
    public AuditQueryResult execute(AuditQuery query) {
        log.debug("Executing audit query: module={}, action={}, keyword={}, diffField={}, page={}, size={}",
                query.getModule(), query.getAction(), query.getKeyword(), query.getDiffField(),
                query.getPage(), query.getSize());

        QueryPlan plan = queryPlanner.plan(query);

        // 执行 COUNT
        long total = countTotal(plan);

        // 执行主查询
        List<AuditEvent> items = executeQuery(plan);

        // 如果有 Diff JOIN，附带变更数据
        Map<String, List<Change>> diffChanges = Collections.emptyMap();
        if (plan.isHasDiffJoin() && !items.isEmpty()) {
            diffChanges = fetchDiffChanges(items);
        }

        return new AuditQueryResult(items, query.getPage(), query.getSize(), total, diffChanges);
    }

    private long countTotal(QueryPlan plan) {
        Long result = jdbcTemplate.queryForObject(
                plan.getCountSql(),
                Long.class,
                plan.getCountParams().toArray()
        );
        return result != null ? result : 0;
    }

    private List<AuditEvent> executeQuery(QueryPlan plan) {
        List<AuditEventEntity> entities = jdbcTemplate.query(
                plan.getSql(),
                rowMapper,
                plan.getParams().toArray()
        );
        return entities.stream()
                .map(AuditEventConverter::toDomain)
                .toList();
    }

    private Map<String, List<Change>> fetchDiffChanges(List<AuditEvent> items) {
        Map<String, List<Change>> result = new LinkedHashMap<>();
        for (AuditEvent event : items) {
            List<Change> changes = changeRepository.findByAuditId(event.getId());
            if (!changes.isEmpty()) {
                result.put(event.getId(), changes);
            }
        }
        return result;
    }
}