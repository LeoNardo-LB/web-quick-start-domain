package org.smm.archetype.infrastructure._shared.client.search.impl;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.infrastructure._shared.client.search.AbstractSearchClient;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class ElasticsearchClientImpl extends AbstractSearchClient {

    private final ElasticsearchOperations operations;

    public ElasticsearchClientImpl(ElasticsearchOperations operations) {
        this.operations = operations;
    }

    @Override
    protected void doIndex(String index, String id, Map<String, Object> document) {
        IndexQuery query = new IndexQuery();
        query.setId(id);
        query.setObject(document);
        operations.index(query, IndexCoordinates.of(index));
    }

    @Override
    protected void doBulkIndex(String index, List<Map.Entry<String, Map<String, Object>>> documents) {
        List<IndexQuery> queries = documents.stream()
            .map(entry -> {
                IndexQuery query = new IndexQuery();
                query.setId(entry.getKey());
                query.setObject(entry.getValue());
                return query;
            })
            .collect(Collectors.toList());

        operations.bulkIndex(queries, IndexCoordinates.of(index));
    }

    @Override
    protected void doDelete(String index, String id) {
        operations.delete(id, IndexCoordinates.of(index));
    }

    @Override
    protected void doBulkDelete(String index, List<String> ids) {
        for (String id : ids) {
            delete(index, id);
        }
    }

    @Override
    protected Map<String, Object> doGet(String index, String id) {
        Map<String, Object> result = operations.get(id, Map.class, IndexCoordinates.of(index));
        return result != null ? new HashMap<>(result) : null;
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
        Query searchQuery = Query.findAll();
        searchQuery.setPageable(org.springframework.data.domain.PageRequest.of(from / size, size));

        SearchHits<Map> searchHits = operations.search(searchQuery, Map.class, IndexCoordinates.of(index));

        Map<String, Object> result = new HashMap<>();
        result.put("took", searchHits.getSearchHits().stream().findFirst().map(SearchHit::getScore).orElse(0.0f));
        result.put("timed_out", false);

        Map<String, Object> shards = new HashMap<>();
        shards.put("total", 1);
        shards.put("successful", 1);
        shards.put("failed", 0);
        result.put("_shards", shards);

        Map<String, Object> hits = new HashMap<>();
        hits.put("total", Map.of(
            "value", searchHits.getTotalHits(),
            "relation", "eq"
        ));

        List<Map<String, Object>> hitsList = searchHits.getSearchHits().stream()
            .map(this::convertSearchHit)
            .collect(Collectors.toList());

        hits.put("hits", hitsList);
        result.put("hits", hits);

        return result;
    }

    @Override
    protected Map<String, Object> doAggregate(String index, String query) {
        return search(index, query, 0, 0);
    }

    @Override
    protected boolean doExistsIndex(String index) {
        IndexOperations indexOps = operations.indexOps(IndexCoordinates.of(index));
        return indexOps.exists();
    }

    @Override
    protected void doCreateIndex(String index, String mapping) {
        IndexOperations indexOps = operations.indexOps(IndexCoordinates.of(index));
        indexOps.create();
        Document mappingDoc = Document.parse(mapping);
        if (mappingDoc.containsKey("mappings")) {
            Map<String, Object> mappings = (Map<String, Object>) mappingDoc.get("mappings");
            if (mappings.containsKey("properties")) {
                Map<String, Object> properties = (Map<String, Object>) mappings.get("properties");
                Document propertiesDoc = Document.create();
                propertiesDoc.putAll(properties);
                indexOps.putMapping(propertiesDoc);
            }
        }
    }

    @Override
    protected void doDeleteIndex(String index) {
        IndexOperations indexOps = operations.indexOps(IndexCoordinates.of(index));
        indexOps.delete();
    }

    @Override
    protected void doRefresh(String index) {
        IndexOperations indexOps = operations.indexOps(IndexCoordinates.of(index));
        indexOps.refresh();
    }

    @Override
    protected Map<String, Object> doVectorSearch(String index, String query) {
        return search(index, query, 0, 10);
    }

    @Override
    protected void doCreateVectorIndex(String index, String vectorField, int dimension, String indexType, String distanceType) {
        Map<String, Object> denseVectorProps = new HashMap<>();
        denseVectorProps.put("type", "dense_vector");
        denseVectorProps.put("dims", dimension);
        denseVectorProps.put("index", true);
        denseVectorProps.put("similarity", distanceType.toLowerCase());

        Map<String, Object> indexOptions = new HashMap<>();
        indexOptions.put("type", indexType.toLowerCase());
        denseVectorProps.put("index_options", indexOptions);

        Map<String, Object> properties = new HashMap<>();
        properties.put(vectorField, denseVectorProps);

        IndexOperations indexOps = operations.indexOps(IndexCoordinates.of(index));
        indexOps.create();
        Document propertiesDoc = Document.create();
        propertiesDoc.putAll(properties);
        indexOps.putMapping(propertiesDoc);
    }

    @Override
    protected List<Map<String, Object>> doBulkVectorSearch(String index, List<String> queries) {
        List<Map<String, Object>> results = new ArrayList<>();
        for (String query : queries) {
            Map<String, Object> result = vectorSearch(index, query);
            results.add(result);
        }
        return results;
    }

    private Map<String, Object> convertSearchHit(SearchHit<Map> hit) {
        Map<String, Object> result = new HashMap<>();
        result.put("_id", hit.getId());
        result.put("_score", hit.getScore());
        result.put("_index", hit.getIndex());
        if (hit.getContent() != null) {
            result.put("_source", hit.getContent());
        }
        return result;
    }
}
