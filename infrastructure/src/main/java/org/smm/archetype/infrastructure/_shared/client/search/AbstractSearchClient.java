package org.smm.archetype.infrastructure._shared.client.search;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain._shared.client.SearchClient;

import java.util.List;
import java.util.Map;

/**
 * ES技术客户端抽象基类
 *
 * <p>实现ES操作的通用流程模板，提供统一的异常处理和日志记录
 *
 * @author Leonardo
 * @since 2026-01-14
 */
@Slf4j
public abstract class AbstractSearchClient implements SearchClient {

    @Override
    public final void index(String index, String id, Map<String, Object> document) {
        try {
            doIndex(index, id, document);
            log.debug("Document indexed: index={}, id={}", index, id);
        } catch (Exception e) {
            log.error("Failed to index document: index={}, id={}", index, id, e);
            throw new RuntimeException("Failed to index document", e);
        }
    }

    @Override
    public final void bulkIndex(String index, List<Map.Entry<String, Map<String, Object>>> documents) {
        try {
            doBulkIndex(index, documents);
            log.debug("Bulk indexed: index={}, count={}", index, documents.size());
        } catch (Exception e) {
            log.error("Failed to bulk index: index={}, count={}", index, documents.size(), e);
            throw new RuntimeException("Failed to bulk index", e);
        }
    }

    @Override
    public final void delete(String index, String id) {
        try {
            doDelete(index, id);
            log.debug("Document deleted: index={}, id={}", index, id);
        } catch (Exception e) {
            log.error("Failed to delete document: index={}, id={}", index, id, e);
            throw new RuntimeException("Failed to delete document", e);
        }
    }

    @Override
    public final void bulkDelete(String index, List<String> ids) {
        try {
            doBulkDelete(index, ids);
            log.debug("Bulk deleted: index={}, count={}", index, ids.size());
        } catch (Exception e) {
            log.error("Failed to bulk delete: index={}, count={}", index, ids.size(), e);
            throw new RuntimeException("Failed to bulk delete", e);
        }
    }

    @Override
    public final Map<String, Object> get(String index, String id) {
        try {
            return doGet(index, id);
        } catch (Exception e) {
            log.error("Failed to get document: index={}, id={}", index, id, e);
            return null;
        }
    }

    @Override
    public final Map<String, Map<String, Object>> bulkGet(String index, List<String> ids) {
        try {
            return doBulkGet(index, ids);
        } catch (Exception e) {
            log.error("Failed to bulk get: index={}, count={}", index, ids.size(), e);
            return Map.of();
        }
    }

    @Override
    public final Map<String, Object> search(String index, String query) {
        return search(index, query, 0, 10);
    }

    @Override
    public final Map<String, Object> search(String index, String query, int from, int size) {
        try {
            Map<String, Object> result = doSearch(index, query, from, size);
            log.debug("Search executed: index={}, from={}, size={}, hits={}",
                index, from, size, result != null ? result.get("hits") : "null");
            return result;
        } catch (Exception e) {
            log.error("Failed to search: index={}, query={}", index, query, e);
            throw new RuntimeException("Failed to search", e);
        }
    }

    @Override
    public final Map<String, Object> aggregate(String index, String query) {
        try {
            Map<String, Object> result = doAggregate(index, query);
            log.debug("Aggregate executed: index={}", index);
            return result;
        } catch (Exception e) {
            log.error("Failed to aggregate: index={}, query={}", index, query, e);
            throw new RuntimeException("Failed to aggregate", e);
        }
    }

    @Override
    public final boolean existsIndex(String index) {
        try {
            return doExistsIndex(index);
        } catch (Exception e) {
            log.error("Failed to check index existence: index={}", index, e);
            return false;
        }
    }

    @Override
    public final void createIndex(String index, String mapping) {
        try {
            doCreateIndex(index, mapping);
            log.info("Index created: index={}", index);
        } catch (Exception e) {
            log.error("Failed to create index: index={}", index, e);
            throw new RuntimeException("Failed to create index", e);
        }
    }

    @Override
    public final void deleteIndex(String index) {
        try {
            doDeleteIndex(index);
            log.info("Index deleted: index={}", index);
        } catch (Exception e) {
            log.error("Failed to delete index: index={}", index, e);
            throw new RuntimeException("Failed to delete index", e);
        }
    }

    @Override
    public final void refresh(String index) {
        try {
            doRefresh(index);
            log.debug("Index refreshed: index={}", index);
        } catch (Exception e) {
            log.error("Failed to refresh index: index={}", index, e);
            throw new RuntimeException("Failed to refresh index", e);
        }
    }

    @Override
    public final Map<String, Object> vectorSearch(String index, String query) {
        try {
            Map<String, Object> result = doVectorSearch(index, query);
            log.debug("Vector search executed: index={}, hits={}",
                index, result != null ? result.get("hits") : "null");
            return result;
        } catch (Exception e) {
            log.error("Failed to execute vector search: index={}, query={}", index, query, e);
            throw new RuntimeException("Failed to execute vector search", e);
        }
    }

    @Override
    public final void createVectorIndex(String index, String vectorField, int dimension, String indexType, String distanceType) {
        try {
            doCreateVectorIndex(index, vectorField, dimension, indexType, distanceType);
            log.info("Vector index created: index={}, field={}, dimension={}, type={}, distance={}",
                index, vectorField, dimension, indexType, distanceType);
        } catch (Exception e) {
            log.error("Failed to create vector index: index={}, field={}", index, vectorField, e);
            throw new RuntimeException("Failed to create vector index", e);
        }
    }

    @Override
    public final List<Map<String, Object>> bulkVectorSearch(String index, List<String> queries) {
        try {
            List<Map<String, Object>> results = doBulkVectorSearch(index, queries);
            log.debug("Bulk vector search executed: index={}, count={}", index, queries.size());
            return results;
        } catch (Exception e) {
            log.error("Failed to execute bulk vector search: index={}, count={}", index, queries.size(), e);
            throw new RuntimeException("Failed to execute bulk vector search", e);
        }
    }

    // ========== 抽象扩展点 ==========

    protected abstract void doIndex(String index, String id, Map<String, Object> document);

    protected abstract void doBulkIndex(String index, List<Map.Entry<String, Map<String, Object>>> documents);

    protected abstract void doDelete(String index, String id);

    protected abstract void doBulkDelete(String index, List<String> ids);

    protected abstract Map<String, Object> doGet(String index, String id);

    protected abstract Map<String, Map<String, Object>> doBulkGet(String index, List<String> ids);

    protected abstract Map<String, Object> doSearch(String index, String query, int from, int size);

    protected abstract Map<String, Object> doAggregate(String index, String query);

    protected abstract boolean doExistsIndex(String index);

    protected abstract void doCreateIndex(String index, String mapping);

    protected abstract void doDeleteIndex(String index);

    protected abstract void doRefresh(String index);

    protected abstract Map<String, Object> doVectorSearch(String index, String query);

    protected abstract void doCreateVectorIndex(String index, String vectorField, int dimension, String indexType, String distanceType);

    protected abstract List<Map<String, Object>> doBulkVectorSearch(String index, List<String> queries);
}
