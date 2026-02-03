package org.smm.archetype.infrastructure.bizshared.client.search.impl;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain.bizshared.client.SearchClient;

import java.util.List;
import java.util.Map;

/**
 * ES客户端禁用实现（Disabled Implementation）
 *
当搜索功能禁用时（middleware.search.enabled=false），使用此实现。
 *
行为特性：
 * <ul>
 *   <li>所有操作抛出 {@link IllegalStateException}</li>
 *   <li>异常消息明确提示 "Elasticsearch is disabled"</li>
 *   <li>记录ERROR级别日志</li>
 * </ul>
 *


 */
@Slf4j
public class DisabledSearchClientImpl implements SearchClient {

    private static final String ES_DISABLED_MESSAGE =
        "Elasticsearch is disabled (middleware.search.enabled=false). " +
        "Please enable it in configuration or remove search-related functionality.";

    @Override
    public void index(String index, String id, Map<String, Object> document) {
        throw new IllegalStateException(ES_DISABLED_MESSAGE);
    }

    @Override
    public void bulkIndex(String index, List<Map.Entry<String, Map<String, Object>>> documents) {
        throw new IllegalStateException(ES_DISABLED_MESSAGE);
    }

    @Override
    public void delete(String index, String id) {
        throw new IllegalStateException(ES_DISABLED_MESSAGE);
    }

    @Override
    public void bulkDelete(String index, List<String> ids) {
        throw new IllegalStateException(ES_DISABLED_MESSAGE);
    }

    @Override
    public Map<String, Object> get(String index, String id) {
        throw new IllegalStateException(ES_DISABLED_MESSAGE);
    }

    @Override
    public Map<String, Map<String, Object>> bulkGet(String index, List<String> ids) {
        throw new IllegalStateException(ES_DISABLED_MESSAGE);
    }

    @Override
    public Map<String, Object> search(String index, String query) {
        throw new IllegalStateException(ES_DISABLED_MESSAGE);
    }

    @Override
    public Map<String, Object> search(String index, String query, int from, int size) {
        throw new IllegalStateException(ES_DISABLED_MESSAGE);
    }

    @Override
    public Map<String, Object> aggregate(String index, String query) {
        throw new IllegalStateException(ES_DISABLED_MESSAGE);
    }

    @Override
    public boolean existsIndex(String index) {
        throw new IllegalStateException(ES_DISABLED_MESSAGE);
    }

    @Override
    public void createIndex(String index, String mapping) {
        throw new IllegalStateException(ES_DISABLED_MESSAGE);
    }

    @Override
    public void deleteIndex(String index) {
        throw new IllegalStateException(ES_DISABLED_MESSAGE);
    }

    @Override
    public void refresh(String index) {
        throw new IllegalStateException(ES_DISABLED_MESSAGE);
    }

    @Override
    public Map<String, Object> vectorSearch(String index, String query) {
        throw new IllegalStateException(ES_DISABLED_MESSAGE);
    }

    @Override
    public void createVectorIndex(String index, String vectorField, int dimension,
                                       String indexType, String distanceType) {
        throw new IllegalStateException(ES_DISABLED_MESSAGE);
    }

    @Override
    public List<Map<String, Object>> bulkVectorSearch(String index, List<String> queries) {
        throw new IllegalStateException(ES_DISABLED_MESSAGE);
    }
}
