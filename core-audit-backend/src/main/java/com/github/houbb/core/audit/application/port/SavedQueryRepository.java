package com.github.houbb.core.audit.application.port;

import java.util.List;
import java.util.Optional;

/**
 * 保存查询仓储接口（Application 层定义）
 */
public interface SavedQueryRepository {

    /**
     * 保存查询
     *
     * @param name      查询名称
     * @param ownerId   创建人 ID
     * @param queryJson AuditQuery DSL JSON
     * @param isPublic  是否共享
     * @return 保存后的记录（含生成的 ID）
     */
    SavedQueryRecord save(String name, String ownerId, String queryJson, boolean isPublic);

    /**
     * 根据 ID 查找
     */
    Optional<SavedQueryRecord> findById(String id);

    /**
     * 查询某用户的所有保存查询（含团队共享）
     *
     * @param ownerId 用户 ID
     * @return 保存查询列表
     */
    List<SavedQueryRecord> findByOwner(String ownerId);

    /**
     * 查询所有公开的保存查询
     */
    List<SavedQueryRecord> findPublic();

    /**
     * 删除保存查询
     *
     * @param id 查询 ID
     * @return 是否删除成功
     */
    boolean deleteById(String id);

    /**
     * 保存查询记录（轻量 POJO）
     */
    final class SavedQueryRecord {
        private final String id;
        private final String name;
        private final String ownerId;
        private final String queryJson;
        private final boolean isPublic;
        private final String createdAt;

        public SavedQueryRecord(String id, String name, String ownerId, String queryJson,
                                boolean isPublic, String createdAt) {
            this.id = id;
            this.name = name;
            this.ownerId = ownerId;
            this.queryJson = queryJson;
            this.isPublic = isPublic;
            this.createdAt = createdAt;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getOwnerId() { return ownerId; }
        public String getQueryJson() { return queryJson; }
        public boolean isPublic() { return isPublic; }
        public String getCreatedAt() { return createdAt; }
    }
}