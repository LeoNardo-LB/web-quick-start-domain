package org.smm.archetype.infrastructure._shared.client.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain._shared.client.EsClient;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Elasticsearch技术客户端实现
 *
 * <p>使用Spring Data Elasticsearch的ElasticsearchClient与Elasticsearch通信
 *
 * @author Leonardo
 * @since 2026-01-14
 */
@Slf4j
public class ElasticsearchClientImpl extends AbstractEsClient {

    private final ElasticsearchClient client;

    public ElasticsearchClientImpl(ElasticsearchClient client) {
        this.client = client;
    }

    @Override
    protected void doIndex(String index, String id, Map<String, Object> document) {
        try {
            IndexRequest<Map<String, Object>> request = IndexRequest.of(b -> b
                .index(index)
                .id(id)
                .document(document)
            );

            client.index(request);
        } catch (IOException e) {
            throw new RuntimeException("Failed to index document", e);
        }
    }

    @Override
    protected void doBulkIndex(String index, List<Map.Entry<String, Map<String, Object>>> documents) {
        try {
            List<BulkOperation> operations = documents.stream()
                .map(entry -> BulkOperation.of(b -> b
                    .index(idx -> idx
                        .index(index)
                        .id(entry.getKey())
                        .document(entry.getValue())
                    )
                ))
                .collect(Collectors.toList());

            BulkRequest bulkRequest = BulkRequest.of(b -> b
                .operations(operations)
            );

            client.bulk(bulkRequest);
        } catch (IOException e) {
            throw new RuntimeException("Failed to bulk index documents", e);
        }
    }

    @Override
    protected void doDelete(String index, String id) {
        try {
            DeleteRequest request = DeleteRequest.of(b -> b
                .index(index)
                .id(id)
            );

            client.delete(request);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete document", e);
        }
    }

    @Override
    protected void doBulkDelete(String index, List<String> ids) {
        try {
            List<BulkOperation> operations = ids.stream()
                .map(id -> BulkOperation.of(b -> b
                    .delete(idx -> idx
                        .index(index)
                        .id(id)
                    )
                ))
                .collect(Collectors.toList());

            BulkRequest bulkRequest = BulkRequest.of(b -> b
                .operations(operations)
            );

            client.bulk(bulkRequest);
        } catch (IOException e) {
            throw new RuntimeException("Failed to bulk delete documents", e);
        }
    }

    @Override
    protected Map<String, Object> doGet(String index, String id) {
        try {
            GetRequest getRequest = GetRequest.of(b -> b
                .index(index)
                .id(id)
            );

            GetResponse<Map> response = client.get(getRequest, Map.class);

            if (response.found()) {
                return response.source();
            }

            return null;
        } catch (IOException e) {
            throw new RuntimeException("Failed to get document", e);
        }
    }

    @Override
    protected Map<String, Map<String, Object>> doBulkGet(String index, List<String> ids) {
        Map<String, Map<String, Object>> results = new HashMap<>();
        for (String id : ids) {
            Map<String, Object> doc = get(index, id);
            if (doc != null) {
                results.put(id, doc);
            }
        }
        return results;
    }

    @Override
    protected Map<String, Object> doSearch(String index, String query, int from, int size) {
        try {
            SearchRequest searchRequest = SearchRequest.of(b -> b
                .index(index)
                .from(from)
                .size(size)
                .withJson(new StringReader(query))
            );

            SearchResponse<Map> response = client.search(searchRequest, Map.class);

            // 转换为Map格式以保持接口兼容性
            Map<String, Object> result = new HashMap<>();
            result.put("took", response.took());
            result.put("timed_out", response.timedOut());
            result.put("_shards", Map.of(
                "total", response.shards().total(),
                "successful", response.shards().successful(),
                "failed", response.shards().failed()
            ));

            Map<String, Object> hits = new HashMap<>();
            hits.put("total", Map.of(
                "value", response.hits().total().value(),
                "relation", response.hits().total().relation().jsonValue()
            ));

            List<Map<String, Object>> hitsList = response.hits().hits().stream()
                .map(this::convertHit)
                .collect(Collectors.toList());

            hits.put("hits", hitsList);
            result.put("hits", hits);

            return result;
        } catch (IOException e) {
            throw new RuntimeException("Failed to search", e);
        }
    }

    @Override
    protected Map<String, Object> doAggregate(String index, String query) {
        // 聚合查询使用search实现，size=0
        return search(index, query, 0, 0);
    }

    @Override
    protected boolean doExistsIndex(String index) {
        try {
            ExistsRequest request = ExistsRequest.of(b -> b.index(index));
            return client.indices().exists(request).value();
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    protected void doCreateIndex(String index, String mapping) {
        try {
            CreateIndexRequest request = CreateIndexRequest.of(b -> b
                .index(index)
                .withJson(new StringReader(mapping))
            );

            client.indices().create(request);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create index", e);
        }
    }

    @Override
    protected void doDeleteIndex(String index) {
        try {
            DeleteIndexRequest request = DeleteIndexRequest.of(b -> b.index(index));
            client.indices().delete(request);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete index", e);
        }
    }

    @Override
    protected void doRefresh(String index) {
        try {
            client.indices().refresh(r -> r.index(index));
        } catch (IOException e) {
            throw new RuntimeException("Failed to refresh index", e);
        }
    }

    @Override
    protected Map<String, Object> doVectorSearch(String index, String query) {
        // 向量搜索使用标准的search实现
        return search(index, query, 0, 10);
    }

    @Override
    protected void doCreateVectorIndex(String index, String vectorField, int dimension, String indexType, String distanceType) {
        try {
            // 构建向量索引映射
            String mapping = buildVectorIndexMapping(vectorField, dimension, indexType, distanceType);

            CreateIndexRequest request = CreateIndexRequest.of(b -> b
                .index(index)
                .withJson(new StringReader(mapping))
            );

            client.indices().create(request);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create vector index", e);
        }
    }

    @Override
    protected List<Map<String, Object>> doBulkVectorSearch(String index, List<String> queries) {
        // 简化实现：逐个执行向量搜索
        List<Map<String, Object>> results = new ArrayList<>();
        for (String query : queries) {
            Map<String, Object> result = vectorSearch(index, query);
            results.add(result);
        }
        return results;
    }

    /**
     * 转换SearchHit为Map格式
     */
    private Map<String, Object> convertHit(Hit<Map> hit) {
        Map<String, Object> result = new HashMap<>();
        result.put("_id", hit.id());
        result.put("_score", hit.score());
        result.put("_index", hit.index());
        if (hit.source() != null) {
            result.put("_source", hit.source());
        }
        return result;
    }

    /**
     * 构建向量索引映射JSON
     */
    private String buildVectorIndexMapping(String vectorField, int dimension, String indexType, String distanceType) {
        return String.format("""
            {
              "mappings": {
                "properties": {
                  "%s": {
                    "type": "dense_vector",
                    "dims": %d,
                    "index": true,
                    "similarity": "%s",
                    "index_options": {
                      "type": "%s"
                    }
                  }
                }
              }
            }
            """, vectorField, dimension, distanceType.toLowerCase(), indexType.toLowerCase());
    }
}
