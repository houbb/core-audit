package com.github.houbb.core.audit.application.query.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.houbb.core.audit.application.domain.query.AuditQuery;
import com.github.houbb.core.audit.application.port.SavedQueryRepository;
import com.github.houbb.core.audit.application.port.SavedQueryRepository.SavedQueryRecord;
import com.github.houbb.core.audit.application.port.SearchHistoryRepository;
import com.github.houbb.core.audit.application.port.SearchHistoryRepository.PopularQueryRecord;
import com.github.houbb.core.audit.application.port.SearchHistoryRepository.SearchHistoryRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 查询历史与保存查询服务
 */
@Service
public class QueryHistoryService {

    private static final Logger log = LoggerFactory.getLogger(QueryHistoryService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final int MAX_RECENT = 20;

    private final SearchHistoryRepository searchHistoryRepository;
    private final SavedQueryRepository savedQueryRepository;

    public QueryHistoryService(SearchHistoryRepository searchHistoryRepository,
                               SavedQueryRepository savedQueryRepository) {
        this.searchHistoryRepository = searchHistoryRepository;
        this.savedQueryRepository = savedQueryRepository;
    }

    /**
     * 记录搜索历史
     */
    public void recordSearch(String userId, AuditQuery query) {
        try {
            String json = objectMapper.writeValueAsString(query);
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
            searchHistoryRepository.save(userId, json, now);

            // 异步清理 30 天前的历史
            searchHistoryRepository.deleteOlderThan(30);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize AuditQuery for search history: {}", e.getMessage());
        }
    }

    /**
     * 获取用户最近搜索历史
     */
    public List<SearchHistoryRecord> getRecentSearches(String userId) {
        return searchHistoryRepository.findRecentByUser(userId, MAX_RECENT);
    }

    /**
     * 保存查询
     */
    public SavedQueryRecord saveQuery(String name, String ownerId, AuditQuery query, boolean isPublic) {
        try {
            String json = objectMapper.writeValueAsString(query);
            return savedQueryRepository.save(name, ownerId, json, isPublic);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize AuditQuery for saved query", e);
        }
    }

    /**
     * 获取已保存查询列表
     */
    public List<SavedQueryRecord> getSavedQueries(String userId) {
        return savedQueryRepository.findByOwner(userId);
    }

    /**
     * 删除已保存查询
     */
    public boolean deleteSavedQuery(String id) {
        return savedQueryRepository.deleteById(id);
    }

    /**
     * 获取热门查询
     */
    public List<PopularQueryRecord> getPopularQueries(int limit) {
        return searchHistoryRepository.findPopular(limit);
    }
}