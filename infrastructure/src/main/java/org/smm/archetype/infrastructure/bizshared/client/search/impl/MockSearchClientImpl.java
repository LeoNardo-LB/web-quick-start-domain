package org.smm.archetype.infrastructure.bizshared.client.search.impl;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain.bizshared.client.SearchClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mock搜索客户端实现,模拟ES搜索操作。
 * 用于测试和开发环境,避免依赖Elasticsearch中间件。
 */
@Slf4j
public class MockSearchClientImpl implements SearchClient {

    /**
     * 索引数据存储
     */
    private final Map<String, Map<String, Map<String, Object>>> indexData = new HashMap<>();

    /**
     * 索引存在标记
     */
    private final Map<String, Boolean> indexExists = new HashMap<>();

    @Override
    public void index(String index, String id, Map<String, Object> document) {
        log.debug("Mock搜索索引: index={}, id={}", index, id);
        indexData.computeIfAbsent(index, k -> new HashMap<>()).put(id, new HashMap<>(document));
    }

    @Override
    public void bulkIndex(String index, List<Map.Entry<String, Map<String, Object>>> documents) {
        log.debug("Mock搜索批量索引: index={}, count={}", index, documents.size());
        Map<String, Map<String, Object>> indexMap = indexData.computeIfAbsent(index, k -> new HashMap<>());
        for (Map.Entry<String, Map<String, Object>> entry : documents) {
            indexMap.put(entry.getKey(), new HashMap<>(entry.getValue()));
        }
    }

    @Override
    public void delete(String index, String id) {
        log.debug("Mock搜索删除: index={}, id={}", index, id);
        Map<String, Map<String, Object>> indexMap = indexData.get(index);
        if (indexMap != null) {
            indexMap.remove(id);
        }
    }

    @Override
    public void bulkDelete(String index, List<String> ids) {
        log.debug("Mock搜索批量删除: index={}, count={}", index, ids.size());
        Map<String, Map<String, Object>> indexMap = indexData.get(index);
        if (indexMap != null) {
            for (String id : ids) {
                indexMap.remove(id);
            }
        }
    }

    @Override
    public Map<String, Object> get(String index, String id) {
        log.debug("Mock搜索获取: index={}, id={}", index, id);
        Map<String, Map<String, Object>> indexMap = indexData.get(index);
        if (indexMap != null) {
            return indexMap.get(id);
        }
        return null;
    }

    @Override
    public Map<String, Map<String, Object>> bulkGet(String index, List<String> ids) {
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
    public Map<String, Object> search(String index, String query) {
        return search(index, query, 0, 10);
    }

    @Override
    public Map<String, Object> search(String index, String query, int from, int size) {
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
    public Map<String, Object> aggregate(String index, String query) {
        log.debug("Mock聚合: index={}, query={}", index, query);
        return Map.of(
            "aggregations", new HashMap<>(),
            "took", 1
        );
    }

    @Override
    public boolean existsIndex(String index) {
        log.debug("Mock搜索索引存在性检查: index={}", index);
        return indexExists.getOrDefault(index, false) || indexData.containsKey(index);
    }

    @Override
    public void createIndex(String index, String mapping) {
        log.debug("Mock搜索创建索引: index={}", index);
        indexExists.put(index, true);
        indexData.putIfAbsent(index, new HashMap<>());
    }

    @Override
    public void deleteIndex(String index) {
        log.debug("Mock搜索删除索引: index={}", index);
        indexData.remove(index);
        indexExists.remove(index);
    }

    @Override
    public void refresh(String index) {
        log.debug("Mock搜索刷新索引: index={}", index);
        // Mock操作,无需实际刷新
    }

    @Override
    public Map<String, Object> vectorSearch(String index, String query) {
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
    public void createVectorIndex(String index, String vectorField, int dimension, String indexType, String distanceType) {
        log.debug("Mock创建向量索引: index={}, field={}, dimension={}, type={}, distance={}",
            index, vectorField, dimension, indexType, distanceType);
        indexExists.put(index + "_vector", true);
    }

    @Override
    public List<Map<String, Object>> bulkVectorSearch(String index, List<String> queries) {
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
