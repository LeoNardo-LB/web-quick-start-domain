package org.smm.archetype.infrastructure.bizshared.client.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smm.archetype.domain.bizshared.client.SearchClient;

import java.util.List;
import java.util.Map;

/**
 * 禁用的ES客户端实现
 */
public class DisabledSearchClientImpl implements SearchClient {

    private static final Logger log                 = LoggerFactory.getLogger(DisabledSearchClientImpl.class);
    private static final String ES_DISABLED_MESSAGE = "Elasticsearch is disabled. Enable it via configuration.";

    @Override
    public Map<String, Object> get(String index, String id) {
        log.info("[Client调用] DisabledSearchClientImpl#get | Elasticsearch禁用 | 0ms | {} | {}",
                Thread.currentThread().getName(),
                index);
        throw new IllegalStateException(ES_DISABLED_MESSAGE);
    }

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
