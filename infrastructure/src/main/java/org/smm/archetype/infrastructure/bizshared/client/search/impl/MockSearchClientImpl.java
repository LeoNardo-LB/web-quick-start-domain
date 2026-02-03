package org.smm.archetype.infrastructure.bizshared.client.search.impl;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.infrastructure.bizshared.client.search.AbstractSearchClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mock搜索客户端实现,模拟ES搜索操作。
 * 用于测试和开发环境,避免依赖Elasticsearch中间件。
 */
@Slf4j
public class MockSearchClientImpl extends AbstractSearchClient {

    /**
     * 索引数据存储
     */
    private final Map<String, Map<String, Map<String, Object>>> indexData = new HashMap<>();

    /**
     * 索引存在标记
     */
    private final Map<String, Boolean> indexExists = new HashMap<>();

    @Override
    protected void doIndex(String index, String id, Map<String, Object> document) {
        log.debug("Mock搜索索引: index={}, id={}", index, id);
        indexData.computeIfAbsent(index, k -> new HashMap<>()).put(id, new HashMap<>(document));
    }

    @Override
    protected void doBulkIndex(String index, List<Map.Entry<String, Map<String, Object>>> documents) {
        log.debug("Mock搜索批量索引: index={}, count={}", index, documents.size());
        Map<String, Map<String, Object>> indexMap = indexData.computeIfAbsent(index, k -> new HashMap<>());
        for (Map.Entry<String, Map<String, Object>> entry : documents) {
            indexMap.put(entry.getKey(), new HashMap<>(entry.getValue()));
        }
    }

    @Override
    protected void doDelete(String index, String id) {
        log.debug("Mock搜索删除: index={}, id={}", index, id);
        Map<String, Map<String, Object>> indexMap = indexData.get(index);
        if (indexMap != null) {
            indexMap.remove(id);
        }
    }

    @Override
    protected void doBulkDelete(String index, List<String> ids) {
        log.debug("Mock搜索批量删除: index={}, count={}", index, ids.size());
        Map<String, Map<String, Object>> indexMap = indexData.get(index);
        if (indexMap != null) {
            for (String id : ids) {
                indexMap.remove(id);
            }
        }
    }

    @Override
    protected Map<String, Object> doGet(String index, String id) {
        log.debug("Mock搜索获取: index={}, id={}", index, id);
        Map<String, Map<String, Object>> indexMap = indexData.get(index);
        if (indexMap != null) {
            return indexMap.get(id);
        }
        return null;
    }

    @Override
    protected Map<String, Map<String, Object>> doBulkGet(String index, List<String> ids) {
        log.debug("Mock搜索批量获取: index={}, count={}", index, ids.size());
        Map<String, Map<String, Object>> result = new HashMap<>();
        Map<String, Map<String, Object>> indexMap = indexData.get(index);
        if (indexMap != null) {
            for (String id : ids) {
                Map<String, Object> doc = indexMap.get(id);
                if (doc != null) {
                    result.put(id, doc);
                }
            }
        }
        return result;
    }

    @Override
    protected Map<String, Object> doSearch(String index, String query, int from, int size) {
        log.debug("Mock搜索: index={}, query={}, from={}, size={}", index, query, from, size);
        Map<String, Map<String, Object>> indexMap = indexData.get(index);
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> hits = new ArrayList<>();

        if (indexMap != null) {
            int count = 0;
            int start = from;
            for (Map.Entry<String, Map<String, Object>> entry : indexMap.entrySet()) {
                if (count >= size) {
                    break;
                }
                if (start > 0) {
                    start--;
                    continue;
                }
                Map<String, Object> hit = new HashMap<>();
                hit.put("_id", entry.getKey());
                hit.put("_source", entry.getValue());
                hits.add(hit);
                count++;
            }
        }

        result.put("hits", new HashMap<>(Map.of(
            "total", new HashMap<>(Map.of("value", hits.size())),
            "hits", hits
        )));
        result.put("took", 1);
        return result;
    }

    @Override
    protected Map<String, Object> doAggregate(String index, String query) {
        log.debug("Mock聚合: index={}, query={}", index, query);
        return Map.of(
            "aggregations", new HashMap<>(),
            "took", 1
        );
    }

    @Override
    protected boolean doExistsIndex(String index) {
        log.debug("Mock搜索索引存在性检查: index={}", index);
        return indexExists.getOrDefault(index, false) || indexData.containsKey(index);
    }

    @Override
    protected void doCreateIndex(String index, String mapping) {
        log.debug("Mock搜索创建索引: index={}", index);
        indexExists.put(index, true);
        indexData.putIfAbsent(index, new HashMap<>());
    }

    @Override
    protected void doDeleteIndex(String index) {
        log.debug("Mock搜索删除索引: index={}", index);
        indexData.remove(index);
        indexExists.remove(index);
    }

    @Override
    protected void doRefresh(String index) {
        log.debug("Mock搜索刷新索引: index={}", index);
        // Mock操作,无需实际刷新
    }

    @Override
    protected Map<String, Object> doVectorSearch(String index, String query) {
        log.debug("Mock向量搜索: index={}, query={}", index, query);
        return Map.of(
            "hits", new HashMap<>(Map.of(
                "total", new HashMap<>(Map.of("value", 0)),
                "hits", new ArrayList<>()
            )),
            "took", 1
        );
    }

    @Override
    protected void doCreateVectorIndex(String index, String vectorField, int dimension, String indexType, String distanceType) {
        log.debug("Mock创建向量索引: index={}, field={}, dimension={}, type={}, distance={}",
            index, vectorField, dimension, indexType, distanceType);
        indexExists.put(index + "_vector", true);
    }

    @Override
    protected List<Map<String, Object>> doBulkVectorSearch(String index, List<String> queries) {
        log.debug("Mock批量向量搜索: index={}, count={}", index, queries.size());
        List<Map<String, Object>> results = new ArrayList<>();
        for (int i = 0; i < queries.size(); i++) {
            results.add(Map.of(
                "hits", new HashMap<>(Map.of(
                    "total", new HashMap<>(Map.of("value", 0)),
                    "hits", new ArrayList<>()
                ))
            ));
        }
        return results;
    }
}
