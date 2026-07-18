package com.github.houbb.core.audit.infrastructure.persistence.repository;

import com.github.houbb.core.audit.application.domain.enterprise.AuditProvider;
import com.github.houbb.core.audit.application.port.AuditProviderRepository;
import com.github.houbb.core.audit.infrastructure.persistence.converter.AuditProviderConverter;
import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditProviderEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * P9 JDBC 实现 AuditProviderRepository
 */
@Repository
public class JdbcAuditProviderRepository implements AuditProviderRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ProviderRowMapper rowMapper = new ProviderRowMapper();

    public JdbcAuditProviderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public AuditProvider save(AuditProvider provider) {
        AuditProviderEntity entity = AuditProviderConverter.toEntity(provider);
        String now = now();

        if (entity.getId() == null || entity.getId().isBlank()) {
            entity.setId(UUID.randomUUID().toString());
        }

        jdbcTemplate.update("DELETE FROM audit_provider WHERE id = ?", entity.getId());

        jdbcTemplate.update(
                "INSERT INTO audit_provider (id, plugin, provider_class, provider_type, version, tenant, " +
                "description, author, config_json, status, installed_at, " +
                "create_time, update_time, create_user, update_user) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                entity.getId(), entity.getPlugin(), entity.getProviderClass(), entity.getProviderType(),
                entity.getVersion(), entity.getTenant(),
                entity.getDescription(), entity.getAuthor(), entity.getConfigJson(),
                entity.getStatus(),
                entity.getInstalledAt() != null ? entity.getInstalledAt() : now,
                entity.getCreateTime() != null ? entity.getCreateTime() : now,
                now, "system", "system"
        );

        return findById(entity.getId()).orElse(provider);
    }

    @Override
    public Optional<AuditProvider> findById(String id) {
        List<AuditProviderEntity> results = jdbcTemplate.query(
                "SELECT * FROM audit_provider WHERE id = ?", rowMapper, id);
        return results.stream().findFirst().map(AuditProviderConverter::toDomain);
    }

    @Override
    public Optional<AuditProvider> findByPluginAndTenant(String plugin, String tenant) {
        List<AuditProviderEntity> results = jdbcTemplate.query(
                "SELECT * FROM audit_provider WHERE plugin = ? AND tenant = ?", rowMapper, plugin, tenant);
        return results.stream().findFirst().map(AuditProviderConverter::toDomain);
    }

    @Override
    public List<AuditProvider> findAllByTenant(String tenant) {
        return jdbcTemplate.query(
                "SELECT * FROM audit_provider WHERE tenant = ? ORDER BY installed_at DESC",
                rowMapper, tenant).stream().map(AuditProviderConverter::toDomain).toList();
    }

    @Override
    public List<AuditProvider> findAllByTypeAndTenant(String providerType, String tenant) {
        return jdbcTemplate.query(
                "SELECT * FROM audit_provider WHERE provider_type = ? AND tenant = ? AND status = 'ACTIVE'",
                rowMapper, providerType, tenant).stream().map(AuditProviderConverter::toDomain).toList();
    }

    @Override
    public List<AuditProvider> findAllActiveByTenant(String tenant) {
        return jdbcTemplate.query(
                "SELECT * FROM audit_provider WHERE tenant = ? AND status = 'ACTIVE' ORDER BY plugin",
                rowMapper, tenant).stream().map(AuditProviderConverter::toDomain).toList();
    }

    @Override
    public long countByTenant(String tenant) {
        Long result = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM audit_provider WHERE tenant = ?", Long.class, tenant);
        return result != null ? result : 0;
    }

    @Override
    public void updateStatus(String id, String status) {
        jdbcTemplate.update(
                "UPDATE audit_provider SET status = ?, update_time = ? WHERE id = ?",
                status, now(), id);
    }

    @Override
    public void deleteById(String id) {
        jdbcTemplate.update("DELETE FROM audit_provider WHERE id = ?", id);
    }

    private String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }

    private static class ProviderRowMapper implements RowMapper<AuditProviderEntity> {
        @Override
        public AuditProviderEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
            AuditProviderEntity e = new AuditProviderEntity();
            e.setId(rs.getString("id"));
            e.setPlugin(rs.getString("plugin"));
            e.setProviderClass(rs.getString("provider_class"));
            e.setProviderType(rs.getString("provider_type"));
            e.setVersion(rs.getString("version"));
            e.setTenant(rs.getString("tenant"));
            e.setDescription(rs.getString("description"));
            e.setAuthor(rs.getString("author"));
            e.setConfigJson(rs.getString("config_json"));
            e.setStatus(rs.getString("status"));
            e.setInstalledAt(rs.getString("installed_at"));
            e.setCreateTime(rs.getString("create_time"));
            e.setUpdateTime(rs.getString("update_time"));
            e.setCreateUser(rs.getString("create_user"));
            e.setUpdateUser(rs.getString("update_user"));
            return e;
        }
    }
}