package com.github.houbb.core.audit.application.port;

import java.util.List;

/**
 * 搜索历史仓储接口（Application 层定义）
 */
public interface SearchHistoryRepository {

    /**
     * 记录一次搜索
     *
     * @param userId    用户 ID
     * @param queryJson AuditQuery DSL JSON
     * @param searchedAt 搜索时间（ISO-8601）
     */
    void save(String userId, String queryJson, String searchedAt);

    /**
     * 获取用户最近的搜索历史
     *
     * @param userId 用户 ID
     * @param limit  返回条数上限
     * @return 搜索历史列表（按时间倒序）
     */
    List<SearchHistoryRecord> findRecentByUser(String userId, int limit);

    /**
     * 删除超过指定天数的历史记录
     *
     * @param days 保留天数
     * @return 删除条数
     */
    int deleteOlderThan(int days);

    /**
     * 获取热门查询（按搜索频次排名）
     *
     * @param limit 返回条数上限
     * @return 热门查询列表
     */
    List<PopularQueryRecord> findPopular(int limit);

    /**
     * 搜索历史记录
     */
    final class SearchHistoryRecord {
        private final String id;
        private final String userId;
        private final String queryJson;
        private final String searchedAt;

        public SearchHistoryRecord(String id, String userId, String queryJson, String searchedAt) {
            this.id = id;
            this.userId = userId;
            this.queryJson = queryJson;
            this.searchedAt = searchedAt;
        }

        public String getId() { return id; }
        public String getUserId() { return userId; }
        public String getQueryJson() { return queryJson; }
        public String getSearchedAt() { return searchedAt; }
    }

    /**
     * 热门查询记录
     */
    final class PopularQueryRecord {
        private final String queryJson;
        private final long count;

        public PopularQueryRecord(String queryJson, long count) {
            this.queryJson = queryJson;
            this.count = count;
        }

        public String getQueryJson() { return queryJson; }
        public long getCount() { return count; }
    }
}