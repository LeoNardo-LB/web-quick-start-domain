package org.smm.archetype.infrastructure.platform.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain.platform.search.SearchErrorCode;
import org.smm.archetype.domain.platform.search.SearchService;
import org.smm.archetype.domain.platform.search.enums.RerankStrategyType;
import org.smm.archetype.domain.platform.search.enums.VectorDistanceType;
import org.smm.archetype.domain.platform.search.enums.VectorIndexType;
import org.smm.archetype.domain.platform.search.query.AiSearchQuery;
import org.smm.archetype.domain.platform.search.query.HybridSearchQuery;
import org.smm.archetype.domain.platform.search.query.SearchAggregation;
import org.smm.archetype.domain.platform.search.query.SearchFilter;
import org.smm.archetype.domain.platform.search.query.SearchQuery;
import org.smm.archetype.domain.platform.search.query.SearchSort;
import org.smm.archetype.domain.platform.search.query.VectorSearchQuery;
import org.smm.archetype.domain.platform.search.result.AggregationBucket;
import org.smm.archetype.domain.platform.search.result.AiSearchHit;
import org.smm.archetype.domain.platform.search.result.AiSearchResult;
import org.smm.archetype.domain.platform.search.result.SearchAggregationResult;
import org.smm.archetype.domain.platform.search.result.SearchHit;
import org.smm.archetype.domain.platform.search.result.SearchResult;
import org.smm.archetype.domain.platform.search.result.VectorSearchHit;
import org.smm.archetype.domain.platform.search.result.VectorSearchResult;
import org.smm.archetype.domain.shared.client.SearchClient;
import org.smm.archetype.domain.shared.exception.SysException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 搜索服务实现，组合ES客户端提供业务搜索。


 */
@Slf4j
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final SearchClient searchClient;
    private final ObjectMapper objectMapper;

    @Override
    public <T> SearchResult<T> search(String index, SearchQuery query, Class<T> documentClass) {
        try {
            // 构建ES查询DSL
            String queryDsl = buildQueryDsl(query);

            // 执行搜索
            Map<String, Object> response = searchClient.search(index, queryDsl, query.getFrom(), query.getSize());

            // 转换结果
            return convertSearchResult(response, documentClass);
        } catch (Exception e) {
            log.error("搜索失败: index={}, 查询词={}", index, query.getKeyword(), e);
            throw new SysException("搜索失败", e, SearchErrorCode.SEARCH_FAILED);
        }
    }

    @Override
    public <T> void index(String index, String id, T document) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> documentMap = objectMapper.convertValue(document, Map.class);
            searchClient.index(index, id, documentMap);
        } catch (Exception e) {
            log.error("索引文档失败: index={}, id={}", index, id, e);
            throw new SysException("索引文档失败", e, SearchErrorCode.INDEX_FAILED);
        }
    }

    @Override
    public <T> void bulkIndex(String index, Map<String, T> documents) {
        try {
            @SuppressWarnings("unchecked")
            List<Map.Entry<String, Map<String, Object>>> documentsList =
                    documents.entrySet().stream()
                            .map(entry -> Map.entry(
                                    entry.getKey(),
                                    (Map<String, Object>) objectMapper.convertValue(
                                            entry.getValue(), Map.class)
                            ))
                            .collect(Collectors.toList());

            searchClient.bulkIndex(index, documentsList);
        } catch (Exception e) {
            log.error("批量索引失败: index={}, 文档数={}", index, documents.size(), e);
            throw new SysException("批量索引失败", e, SearchErrorCode.BATCH_INDEX_FAILED);
        }
    }

    @Override
    public void delete(String index, String id) {
        searchClient.delete(index, id);
    }

    @Override
    public void bulkDelete(String index, List<String> ids) {
        searchClient.bulkDelete(index, ids);
    }

    @Override
    public <T> T get(String index, String id, Class<T> documentClass) {
        try {
            Map<String, Object> source = searchClient.get(index, id);
            if (source == null) {
                return null;
            }
            return objectMapper.convertValue(source, documentClass);
        } catch (Exception e) {
            log.error("获取文档失败: index={}, id={}", index, id, e);
            throw new SysException("获取文档失败", e, SearchErrorCode.GET_DOCUMENT_FAILED);
        }
    }

    @Override
    public <T> Map<String, T> bulkGet(String index, List<String> ids, Class<T> documentClass) {
        try {
            Map<String, Map<String, Object>> results = searchClient.bulkGet(index, ids);

            Map<String, T> documents = new HashMap<>();
            for (Map.Entry<String, Map<String, Object>> entry : results.entrySet()) {
                T document = objectMapper.convertValue(entry.getValue(), documentClass);
                documents.put(entry.getKey(), document);
            }
            return documents;
        } catch (Exception e) {
            log.error("批量获取失败: index={}, count={}", index, ids.size(), e);
            throw new SysException("批量获取失败", e, SearchErrorCode.BATCH_GET_FAILED);
        }
    }

    @Override
    public void refresh(String index) {
        searchClient.refresh(index);
    }

    @Override
    public boolean existsIndex(String index) {
        return searchClient.existsIndex(index);
    }

    @Override
    public void createIndex(String index, String mapping) {
        searchClient.createIndex(index, mapping);
    }

    @Override
    public void deleteIndex(String index) {
        searchClient.deleteIndex(index);
    }

    // ========== 私有方法 ==========

    /**
     * 构建ES查询DSL
     */
    private String buildQueryDsl(SearchQuery query) {
        try {
            var queryNode = objectMapper.createObjectNode();

            // 构建query部分
            var queryObj = objectMapper.createObjectNode();

            if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
                // 全文搜索
                var matchObj = objectMapper.createObjectNode();
                var queryField = matchObj.putObject("_all");
                queryField.put("query", query.getKeyword());
                queryField.put("operator", "or");
                queryObj.set("match", matchObj);
            } else {
                // match_all
                queryObj.putObject("match_all");
            }

            queryNode.set("query", queryObj);

            // 添加过滤条件
            if (!query.getFilters().isEmpty()) {
                var filterList = objectMapper.createArrayNode();
                for (SearchFilter filter : query.getFilters()) {
                    filterList.add(buildFilterCondition(filter));
                }
                queryNode.set("post_filter", objectMapper.createObjectNode().set("bool",
                        objectMapper.createObjectNode().set("must", filterList)));
            }

            // 添加排序
            if (!query.getSorts().isEmpty()) {
                var sortArray = objectMapper.createArrayNode();
                for (SearchSort sort : query.getSorts()) {
                    var sortObj = objectMapper.createObjectNode();
                    var sortField = sortObj.putObject(sort.getField());
                    sortField.put("order", sort.getOrder().name().toLowerCase());
                    sortArray.add(sortObj);
                }
                queryNode.set("sort", sortArray);
            }

            // 添加聚合
            if (!query.getAggregations().isEmpty()) {
                var aggsNode = objectMapper.createObjectNode();
                for (SearchAggregation aggregation : query.getAggregations()) {
                    aggsNode.set(aggregation.getName(), buildAggregation(aggregation));
                }
                queryNode.set("aggs", aggsNode);
            }

            return objectMapper.writeValueAsString(queryNode);
        } catch (Exception e) {
            log.error("构建查询DSL失败", e);
            throw new SysException("构建查询DSL失败", e, SearchErrorCode.DSL_BUILD_FAILED);
        }
    }

    /**
     * 构建过滤条件
     */
    private JsonNode buildFilterCondition(SearchFilter filter) {
        var condition = objectMapper.createObjectNode();

        switch (filter.getOperator()) {
            case EQ:
                var termObj = objectMapper.createObjectNode();
                termObj.put(filter.getField(), filter.getValue().toString());
                condition.set("term", termObj);
                break;
            case NE:
                var mustNotObj = objectMapper.createObjectNode();
                var termObj2 = objectMapper.createObjectNode();
                termObj2.put(filter.getField(), filter.getValue().toString());
                mustNotObj.set("term", termObj2);
                condition.set("must_not", termObj2);
                break;
            case GT:
                var rangeObj1 = objectMapper.createObjectNode();
                var fieldRange1 = rangeObj1.putObject(filter.getField());
                fieldRange1.put("gt", filter.getValue().toString());
                condition.set("range", rangeObj1);
                break;
            case GTE:
                var rangeObj2 = objectMapper.createObjectNode();
                var fieldRange2 = rangeObj2.putObject(filter.getField());
                fieldRange2.put("gte", filter.getValue().toString());
                condition.set("range", rangeObj2);
                break;
            case LT:
                var rangeObj3 = objectMapper.createObjectNode();
                var fieldRange3 = rangeObj3.putObject(filter.getField());
                fieldRange3.put("lt", filter.getValue().toString());
                condition.set("range", rangeObj3);
                break;
            case LTE:
                var rangeObj4 = objectMapper.createObjectNode();
                var fieldRange4 = rangeObj4.putObject(filter.getField());
                fieldRange4.put("lte", filter.getValue().toString());
                condition.set("range", rangeObj4);
                break;
            case IN:
                var termsObj = objectMapper.createObjectNode();
                var termsArray = objectMapper.createArrayNode();
                if (filter.getValue() instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Object> values = (List<Object>) filter.getValue();
                    for (Object v : values) {
                        termsArray.add(v.toString());
                    }
                }
                termsObj.set(filter.getField(), termsArray);
                condition.set("terms", termsObj);
                break;
            case EXISTS:
                var existsObj = objectMapper.createObjectNode();
                existsObj.put("field", filter.getField());
                condition.set("exists", existsObj);
                break;
            case NOT_EXISTS:
                var mustNotObj2 = objectMapper.createObjectNode();
                var existsObj2 = objectMapper.createObjectNode();
                existsObj2.put("field", filter.getField());
                mustNotObj2.set("exists", existsObj2);
                condition.set("must_not", existsObj2);
                break;
            default:
                // 默认使用term查询
                var defaultTermObj = objectMapper.createObjectNode();
                defaultTermObj.put(filter.getField(), filter.getValue().toString());
                condition.set("term", defaultTermObj);
                break;
        }

        return condition;
    }

    /**
     * 构建聚合查询
     */
    private JsonNode buildAggregation(SearchAggregation aggregation) {
        var aggrNode = objectMapper.createObjectNode();

        switch (aggregation.getType()) {
            case TERMS:
                var termsAgg = objectMapper.createObjectNode();
                termsAgg.put("field", aggregation.getField());
                termsAgg.put("size", aggregation.getSize() > 0 ? aggregation.getSize() : 10);
                aggrNode.set("terms", termsAgg);
                break;
            case SUM:
                var sumAgg = objectMapper.createObjectNode();
                sumAgg.put("field", aggregation.getField());
                aggrNode.set("sum", sumAgg);
                break;
            case AVG:
                var avgAgg = objectMapper.createObjectNode();
                avgAgg.put("field", aggregation.getField());
                aggrNode.set("avg", avgAgg);
                break;
            case MAX:
                var maxAgg = objectMapper.createObjectNode();
                maxAgg.put("field", aggregation.getField());
                aggrNode.set("max", maxAgg);
                break;
            case MIN:
                var minAgg = objectMapper.createObjectNode();
                minAgg.put("field", aggregation.getField());
                aggrNode.set("min", minAgg);
                break;
            case STATS:
                var statsAgg = objectMapper.createObjectNode();
                statsAgg.put("field", aggregation.getField());
                aggrNode.set("stats", statsAgg);
                break;
            default:
                break;
        }

        return aggrNode;
    }

    /**
     * 转换搜索结果
     */
    @SuppressWarnings("unchecked")
    private <T> SearchResult<T> convertSearchResult(Map<String, Object> response, Class<T> documentClass) {
        var hits = (Map<String, Object>) response.get("hits");
        var totalObj = (Map<String, Object>) hits.get("total");
        long totalHits = ((Number) totalObj.get("value")).longValue();
        float maxScore = hits.containsKey("max_score") ?
                                 ((Number) hits.get("max_score")).floatValue() : 0.0f;

        var hitsList = (List<Map<String, Object>>) hits.get("hits");
        List<SearchHit<T>> searchHits = new ArrayList<>();

        for (Map<String, Object> hit : hitsList) {
            String id = (String) hit.get("_id");
            float score = ((Number) hit.get("_score")).floatValue();
            Map<String, Object> source = (Map<String, Object>) hit.get("_source");

            T document = objectMapper.convertValue(source, documentClass);

            SearchHit<T> searchHit = SearchHit.<T>builder()
                                             .setId(id)
                                             .setScore(score)
                                             .setDocument(document)
                                             .build();
            searchHits.add(searchHit);
        }

        // 转换聚合结果
        List<SearchAggregationResult> aggregations = new ArrayList<>();
        if (response.containsKey("aggregations")) {
            var aggrsObj = (Map<String, Object>) response.get("aggregations");
            for (Map.Entry<String, Object> entry : aggrsObj.entrySet()) {
                String aggrName = entry.getKey();
                var aggrData = (Map<String, Object>) entry.getValue();

                if (aggrData.containsKey("buckets")) {
                    // Terms聚合
                    var bucketsList = (List<Map<String, Object>>) aggrData.get("buckets");
                    List<AggregationBucket> buckets = new ArrayList<>();

                    for (Map<String, Object> bucketData : bucketsList) {
                        String key = (String) bucketData.get("keyAsString");
                        if (key == null) {
                            key = bucketData.get("key").toString();
                        }
                        long docCount = ((Number) bucketData.get("doc_count")).longValue();

                        AggregationBucket bucket = AggregationBucket.builder()
                                                           .key(key)
                                                           .docCount(docCount)
                                                           .build();
                        buckets.add(bucket);
                    }

                    SearchAggregationResult aggrResult = SearchAggregationResult.builder()
                                                                 .name(aggrName)
                                                                 .buckets(buckets)
                                                                 .build();
                    aggregations.add(aggrResult);
                } else if (aggrData.containsKey("value")) {
                    // 单值聚合（SUM, AVG, MAX, MIN）
                    Number value = (Number) aggrData.get("value");
                    SearchAggregationResult aggrResult = SearchAggregationResult.builder()
                                                                 .name(aggrName)
                                                                 .value(value != null ? value.doubleValue() : null)
                                                                 .build();
                    aggregations.add(aggrResult);
                }
            }
        }

        long took = response.containsKey("took") ?
                            ((Number) response.get("took")).longValue() : 0;

        return SearchResult.<T>builder()
            .totalHits(totalHits)
            .maxScore(maxScore)
            .hits(searchHits)
            .aggregations(aggregations)
            .took(took)
            .build();
    }

    @Override
    public <T> VectorSearchResult<T> vectorSearch(String index, VectorSearchQuery query, Class<T> documentClass) {
        try {
            // 构建kNN查询DSL
            String queryDsl = buildKnnQueryDsl(query);

            // 执行向量搜索
            Map<String, Object> response = searchClient.vectorSearch(index, queryDsl);

            // 转换结果
            return convertVectorSearchResult(response, documentClass);
        } catch (Exception e) {
            log.error("执行向量搜索失败: index={}", index, e);
            throw new SysException("执行向量搜索失败", e, SearchErrorCode.VECTOR_SEARCH_FAILED);
        }
    }

    @Override
    public void createVectorIndex(String index, String vectorField, int dimension,
                                 VectorIndexType indexType, VectorDistanceType distanceType) {
        try {
            String indexTypeStr = indexType.name().toLowerCase();
            String distanceTypeStr = convertDistanceType(distanceType);

            searchClient.createVectorIndex(index, vectorField, dimension, indexTypeStr, distanceTypeStr);
        } catch (Exception e) {
            log.error("创建向量索引失败: index={}, field={}", index, vectorField, e);
            throw new SysException("创建向量索引失败", e, SearchErrorCode.VECTOR_INDEX_FAILED);
        }
    }

    /**
     * 构建kNN查询DSL
     */
    private String buildKnnQueryDsl(VectorSearchQuery query) throws Exception {
        var rootNode = objectMapper.createObjectNode();
        var knnNode = objectMapper.createObjectNode();

        // 设置k值
        knnNode.put("k", query.getK());

        // 设置向量字段
        knnNode.put("field", query.getVectorField());

        // 设置查询向量
        var queryVector = objectMapper.createArrayNode();
        for (Float v : query.getVector()) {
            queryVector.add(v);
        }
        knnNode.set("query_vector", queryVector);

        // 设置过滤条件
        if (query.getFilters() != null && !query.getFilters().isEmpty()) {
            var filterNode = objectMapper.createObjectNode();
            var boolNode = objectMapper.createObjectNode();
            var mustArray = boolNode.putArray("must");

            for (SearchFilter filter : query.getFilters()) {
                mustArray.add(buildFilterCondition(filter));
            }

            filterNode.set("bool", boolNode);
            rootNode.set("filter", filterNode);
        }

        // 设置参数
        if (query.getNprobes() != null) {
            knnNode.put("nprobes", query.getNprobes());
        }
        if (query.getEfSearch() != null) {
            knnNode.put("ef_search", query.getEfSearch());
        }

        rootNode.set("knn", knnNode);

        return objectMapper.writeValueAsString(rootNode);
    }

    /**
     * 转换距离类型枚举为ES字符串
     */
    private String convertDistanceType(VectorDistanceType distanceType) {
        return switch (distanceType) {
            case COSINE -> "cosine";
            case L2 -> "l2_norm";
            case DOT_PRODUCT -> "dot_product";
        };
    }

    /**
     * 转换向量搜索结果
     */
    @SuppressWarnings("unchecked")
    private <T> VectorSearchResult<T> convertVectorSearchResult(Map<String, Object> response, Class<T> documentClass) {
        var hits = (Map<String, Object>) response.get("hits");
        var hitsList = (List<Map<String, Object>>) hits.get("hits");

        List<VectorSearchHit<T>> vectorHits = new ArrayList<>();

        for (Map<String, Object> hit : hitsList) {
            String id = (String) hit.get("_id");
            float score = ((Number) hit.get("_score")).floatValue();
            Map<String, Object> source = (Map<String, Object>) hit.get("_source");

            T document = objectMapper.convertValue(source, documentClass);

            VectorSearchHit<T> vectorHit = VectorSearchHit.<T>builder()
                                                   .id(id)
                                                   .score(score)
                                                   .document(document)
                                                   .distance(null) // ES返回的是score，不是原始距离
                                                   .extraInfo(null) // 简化实现，不包含额外信息
                                                   .build();
            vectorHits.add(vectorHit);
        }

        long took = response.containsKey("took") ?
                            ((Number) response.get("took")).longValue() : 0;

        return VectorSearchResult.<T>builder()
            .hits(vectorHits)
            .took(took)
            .build();
    }

    /**
     * 构建简单的match查询
     */
    private JsonNode buildMatchQuery(String queryText, List<SearchFilter> filters) throws Exception {
        var queryObj = objectMapper.createObjectNode();

        if (queryText != null && !queryText.isBlank()) {
            var matchObj = objectMapper.createObjectNode();
            var queryField = matchObj.putObject("_all");
            queryField.put("query", queryText);
            queryField.put("operator", "or");
            queryObj.set("match", matchObj);
        } else {
            queryObj.putObject("match_all");
        }

        // 添加过滤条件
        if (filters != null && !filters.isEmpty()) {
            var boolQuery = objectMapper.createObjectNode();
            boolQuery.set("must", queryObj);

            var filterArray = objectMapper.createArrayNode();
            for (SearchFilter filter : filters) {
                filterArray.add(buildFilterCondition(filter));
            }
            boolQuery.set("filter", filterArray);

            var wrappedQuery = objectMapper.createObjectNode();
            wrappedQuery.set("bool", boolQuery);
            return wrappedQuery;
        }

        return queryObj;
    }

    /**
     * 构建AI搜索查询DSL
     */
    private String buildAiSearchQueryDsl(AiSearchQuery query) throws Exception {
        var rootNode = objectMapper.createObjectNode();

        switch (query.getRerankStrategy()) {
            case NONE -> {
                // 简单BM25查询
                var queryObj = buildMatchQuery(query.getQueryText(), query.getFilters());
                rootNode.set("query", queryObj);
            }
            case SCORE_WEIGHTED -> {
                // 使用function_score实现加权
                var functionScoreNode = buildFunctionScoreForWeighted(query);
                rootNode.set("query", functionScoreNode);
            }
            case RRF -> {
                // 简化实现：使用BM25分数作为基础
                var queryObj = buildMatchQuery(query.getQueryText(), query.getFilters());
                rootNode.set("query", queryObj);
            }
            case AI_MODEL -> {
                // AI模型重排序：先用BM25获取候选
                var queryObj = buildMatchQuery(query.getQueryText(), query.getFilters());
                rootNode.set("query", queryObj);
            }
        }

        rootNode.put("from", query.getFrom());
        rootNode.put("size", query.getSize());

        return objectMapper.writeValueAsString(rootNode);
    }

    /**
     * 构建SCORE_WEIGHTED的function_score查询
     */
    private JsonNode buildFunctionScoreForWeighted(AiSearchQuery query) throws Exception {
        var functionScoreObj = objectMapper.createObjectNode();

        // 基础查询（BM25）
        var queryObj = buildMatchQuery(query.getQueryText(), query.getFilters());
        functionScoreObj.set("query", queryObj);

        // score_mode: multiply（分数相乘）
        functionScoreObj.put("score_mode", "multiply");
        // boost_mode: replace（替换原始分数）
        functionScoreObj.put("boost_mode", "replace");

        // 单个function：对BM25分数加权
        var functionsArray = objectMapper.createArrayNode();
        var bm25Function = objectMapper.createObjectNode();
        bm25Function.put("weight", query.getBm25Weight());
        var matchAllFilter = objectMapper.createObjectNode();
        matchAllFilter.putObject("match_all");
        bm25Function.set("filter", matchAllFilter);
        functionsArray.add(bm25Function);

        functionScoreObj.set("functions", functionsArray);

        return objectMapper.createObjectNode().set("function_score", functionScoreObj);
    }

    /**
     * 构建混合搜索查询DSL（BM25 + kNN）
     */
    private String buildHybridSearchQueryDsl(HybridSearchQuery query) throws Exception {
        var rootNode = objectMapper.createObjectNode();

        // 使用bool查询结合BM25和kNN
        var boolObj = objectMapper.createObjectNode();

        // should子句：BM25查询 + kNN查询
        var shouldArray = objectMapper.createArrayNode();

        // 1. BM25查询
        var bm25Query = objectMapper.createObjectNode();
        var matchObj = bm25Query.putObject("match");
        matchObj.putObject("_all").put("query", query.getQueryText());
        shouldArray.add(bm25Query);

        // 2. kNN查询
        var knnQuery = objectMapper.createObjectNode();
        var knnObj = knnQuery.putObject("knn");
        knnObj.put("field", query.getVectorField());
        knnObj.put("k", query.getK());

        // 设置查询向量
        var queryVector = objectMapper.createArrayNode();
        for (Float v : query.getQueryVector()) {
            queryVector.add(v);
        }
        knnObj.set("query_vector", queryVector);
        knnObj.put("num_candidates", query.getK() * 10);

        shouldArray.add(knnQuery);
        boolObj.set("should", shouldArray);

        rootNode.set("query", objectMapper.createObjectNode().set("bool", boolObj));
        rootNode.put("from", query.getFrom());
        rootNode.put("size", query.getSize());

        return objectMapper.writeValueAsString(rootNode);
    }

    /**
     * 转换AI搜索结果
     */
    @SuppressWarnings("unchecked")
    private <T> AiSearchResult<T> convertAiSearchResult(
            Map<String, Object> response,
            Class<T> documentClass,
            RerankStrategyType rerankStrategy) {

        var hits = (Map<String, Object>) response.get("hits");
        var totalObj = (Map<String, Object>) hits.get("total");
        long totalHits = ((Number) totalObj.get("value")).longValue();

        var hitsList = (List<Map<String, Object>>) hits.get("hits");
        List<AiSearchHit<T>> aiHits = new ArrayList<>();

        for (Map<String, Object> hit : hitsList) {
            String id = (String) hit.get("_id");
            float score = ((Number) hit.get("_score")).floatValue();
            Map<String, Object> source = (Map<String, Object>) hit.get("_source");

            T document = objectMapper.convertValue(source, documentClass);

            var hitBuilder = AiSearchHit.<T>builder()
                                     .id(id)
                                     .score(score)
                                     .document(document);

            // 根据策略填充额外字段
            if (rerankStrategy == RerankStrategyType.SCORE_WEIGHTED) {
                hitBuilder.bm25Score(score);
                hitBuilder.aiScore(null);
            } else if (rerankStrategy == RerankStrategyType.RRF) {
                hitBuilder.bm25Score(null);
                hitBuilder.aiScore(null);
            }

            aiHits.add(hitBuilder.build());
        }

        long took = response.containsKey("took") ?
                            ((Number) response.get("took")).longValue() : 0;

        return AiSearchResult.<T>builder()
            .hits(aiHits)
            .totalHits(totalHits)
            .took(took)
            .expandedTerms(null)
            .build();
    }

    @Override
    public <T> org.smm.archetype.domain.platform.search.result.AiSearchResult<T> aiSearch(
            String index,
            org.smm.archetype.domain.platform.search.query.AiSearchQuery query,
            Class<T> documentClass) {
        try {
            // 构建AI搜索查询DSL（包含重排序策略）
            String queryDsl = buildAiSearchQueryDsl(query);

            // 执行搜索，委托给具体的EsClient实现
            // ElasticsearchClientImpl会使用ES的function_score
            Map<String, Object> response = searchClient.search(index, queryDsl, query.getFrom(), query.getSize());

            // 转换结果
            return convertAiSearchResult(response, documentClass, query.getRerankStrategy());

        } catch (Exception e) {
            log.error("执行AI搜索失败: index={}", index, e);
            throw new SysException("执行AI搜索失败", e, SearchErrorCode.AI_SEARCH_FAILED);
        }
    }

    @Override
    public <T> SearchResult<T> hybridSearch(
            String index,
            org.smm.archetype.domain.platform.search.query.HybridSearchQuery query,
            Class<T> documentClass) {
        try {
            // 构建混合搜索查询DSL（BM25 + kNN）
            String queryDsl = buildHybridSearchQueryDsl(query);

            // 执行搜索
            Map<String, Object> response = searchClient.search(index, queryDsl, query.getFrom(), query.getSize());

            // 转换结果
            return convertSearchResult(response, documentClass);

        } catch (Exception e) {
            log.error("执行混合搜索失败: index={}", index, e);
            throw new SysException("执行混合搜索失败", e, SearchErrorCode.HYBRID_SEARCH_FAILED);
        }
    }
}
