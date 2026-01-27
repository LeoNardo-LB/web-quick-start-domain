package org.smm.archetype.infrastructure.bizshared.client.search.impl;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.infrastructure.bizshared.client.search.AbstractSearchClient;

import java.util.List;
import java.util.Map;

/**
 * ES客户端禁用实现（Disabled Implementation）
 *
 * <p>当搜索功能禁用时（middleware.search.enabled=false），使用此实现。
 *
 * <p>行为特性：
 * <ul>
 *   <li>所有操作抛出 {@link IllegalStateException}</li>
 *   <li>异常消息明确提示 "Elasticsearch is disabled"</li>
 *   <li>记录ERROR级别日志</li>
 * </ul>
 *
 * @author Leonardo
 * @since 2026-01-15
 */
@Slf4j
public class DisabledSearchClientImpl extends AbstractSearchClient {

    private static final String ES_DISABLED_MESSAGE =
        "Elasticsearch is disabled (middleware.search.enabled=false). " +
        "Please enable it in configuration or remove search-related functionality.";

    @Override
    protected void doIndex(String index, String id, Map<String, Object> document) {
        throw new IllegalStateException(ES_DISABLED_MESSAGE);
    }

    @Override
    protected void doBulkIndex(String index, List<Map.Entry<String, Map<String, Object>>> documents) {
        throw new IllegalStateException(ES_DISABLED_MESSAGE);
    }

    @Override
    protected void doDelete(String index, String id) {
        throw new IllegalStateException(ES_DISABLED_MESSAGE);
    }

    @Override
    protected void doBulkDelete(String index, List<String> ids) {
        throw new IllegalStateException(ES_DISABLED_MESSAGE);
    }

    @Override
    protected Map<String, Object> doGet(String index, String id) {
        throw new IllegalStateException(ES_DISABLED_MESSAGE);
    }

    @Override
    protected Map<String, Map<String, Object>> doBulkGet(String index, List<String> ids) {
        throw new IllegalStateException(ES_DISABLED_MESSAGE);
    }

    @Override
    protected Map<String, Object> doSearch(String index, String query, int from, int size) {
        throw new IllegalStateException(ES_DISABLED_MESSAGE);
    }

    @Override
    protected Map<String, Object> doAggregate(String index, String query) {
        throw new IllegalStateException(ES_DISABLED_MESSAGE);
    }

    @Override
    protected boolean doExistsIndex(String index) {
        throw new IllegalStateException(ES_DISABLED_MESSAGE);
    }

    @Override
    protected void doCreateIndex(String index, String mapping) {
        throw new IllegalStateException(ES_DISABLED_MESSAGE);
    }

    @Override
    protected void doDeleteIndex(String index) {
        throw new IllegalStateException(ES_DISABLED_MESSAGE);
    }

    @Override
    protected void doRefresh(String index) {
        throw new IllegalStateException(ES_DISABLED_MESSAGE);
    }

    @Override
    protected Map<String, Object> doVectorSearch(String index, String query) {
        throw new IllegalStateException(ES_DISABLED_MESSAGE);
    }

    @Override
    protected void doCreateVectorIndex(String index, String vectorField, int dimension,
                                       String indexType, String distanceType) {
        throw new IllegalStateException(ES_DISABLED_MESSAGE);
    }

    @Override
    protected List<Map<String, Object>> doBulkVectorSearch(String index, List<String> queries) {
        throw new IllegalStateException(ES_DISABLED_MESSAGE);
    }
}
