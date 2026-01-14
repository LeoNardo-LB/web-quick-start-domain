package org.smm.archetype.app._example.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.app._example.search.command.SearchCommand;
import org.smm.archetype.app._example.search.command.AiSearchCommand;
import org.smm.archetype.app._example.search.command.VectorSearchCommand;
import org.smm.archetype.app._example.search.command.HybridSearchCommand;
import org.smm.archetype.app._example.search.dto.PageResultDTO;
import org.smm.archetype.app._example.search.dto.SearchAggregationResultDTO;
import org.smm.archetype.app._example.search.dto.SearchHitDTO;
import org.smm.archetype.app._example.search.dto.SearchResultDTO;
import org.smm.archetype.app._example.search.dto.VectorSearchResultDTO;
import org.smm.archetype.app._example.search.dto.AiSearchResultDTO;
import org.smm.archetype.domain.common.search.SearchService;
import org.smm.archetype.domain.common.search.enums.AggregationType;
import org.smm.archetype.domain.common.search.enums.FilterOperator;
import org.smm.archetype.domain.common.search.enums.RerankStrategyType;
import org.smm.archetype.domain.common.search.enums.SearchStrategy;
import org.smm.archetype.domain.common.search.enums.SortOrder;
import org.smm.archetype.domain.common.search.enums.VectorDistanceType;
import org.smm.archetype.domain.common.search.enums.VectorIndexType;
import org.smm.archetype.domain.common.search.enums.AiSearchModelType;
import org.smm.archetype.domain.common.search.query.SearchAggregation;
import org.smm.archetype.domain.common.search.query.SearchFilter;
import org.smm.archetype.domain.common.search.query.SearchQuery;
import org.smm.archetype.domain.common.search.query.SearchSort;
import org.smm.archetype.domain.common.search.query.VectorSearchQuery;
import org.smm.archetype.domain.common.search.query.AiSearchQuery;
import org.smm.archetype.domain.common.search.query.HybridSearchQuery;
import org.smm.archetype.domain.common.search.result.AggregationBucket;
import org.smm.archetype.domain.common.search.result.SearchAggregationResult;
import org.smm.archetype.domain.common.search.result.SearchHit;
import org.smm.archetype.domain.common.search.result.SearchResult;
import org.smm.archetype.domain.common.search.result.VectorSearchResult;
import org.smm.archetype.domain.common.search.result.VectorSearchHit;
import org.smm.archetype.domain.common.search.result.AiSearchResult;
import org.smm.archetype.domain.common.search.result.AiSearchHit;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 搜索应用服务
 *
 * <p>负责搜索用例的编排和DTO转换
 *
 * @author Leonardo
 * @since 2026-01-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchAppService {

    private final SearchService searchService;

    /**
     * 通用搜索
     *
     * @param index 索引名称
     * @param command 搜索命令
     * @param documentClass 文档类型
     * @param <T> 文档泛型
     * @return 分页搜索结果
     */
    public <T> PageResultDTO<T> search(String index, SearchCommand command, Class<T> documentClass) {
        log.info("Searching: index={}, command={}", index, command);

        // 1. 转换命令对象为领域查询对象
        SearchQuery query = convertToSearchQuery(command);

        // 2. 调用领域服务执行搜索
        SearchResult<T> searchResult = searchService.search(index, query, documentClass);

        // 3. 转换领域结果为DTO
        return convertToPageResult(searchResult, command.getPageNo(), command.getPageSize());
    }

    /**
     * 索引文档
     *
     * @param index 索引名称
     * @param id 文档ID
     * @param document 文档对象
     * @param <T> 文档泛型
     */
    public <T> void index(String index, String id, T document) {
        log.info("Indexing document: index={}, id={}", index, id);
        searchService.index(index, id, document);
    }

    /**
     * 批量索引文档
     *
     * @param index 索引名称
     * @param documents 文档Map
     * @param <T> 文档泛型
     */
    public <T> void bulkIndex(String index, java.util.Map<String, T> documents) {
        log.info("Bulk indexing: index={}, count={}", index, documents.size());
        searchService.bulkIndex(index, documents);
    }

    /**
     * 删除文档
     *
     * @param index 索引名称
     * @param id 文档ID
     */
    public void delete(String index, String id) {
        log.info("Deleting document: index={}, id={}", index, id);
        searchService.delete(index, id);
    }

    /**
     * 获取文档
     *
     * @param index 索引名称
     * @param id 文档ID
     * @param documentClass 文档类型
     * @param <T> 文档泛型
     * @return 文档对象
     */
    public <T> T get(String index, String id, Class<T> documentClass) {
        log.debug("Getting document: index={}, id={}", index, id);
        return searchService.get(index, id, documentClass);
    }

    /**
     * 刷新索引
     *
     * @param index 索引名称
     */
    public void refresh(String index) {
        log.info("Refreshing index: index={}", index);
        searchService.refresh(index);
    }

    /**
     * 创建索引
     *
     * @param index 索引名称
     * @param mapping 索引映射
     */
    public void createIndex(String index, String mapping) {
        log.info("Creating index: index={}", index);
        searchService.createIndex(index, mapping);
    }

    /**
     * 删除索引
     *
     * @param index 索引名称
     */
    public void deleteIndex(String index) {
        log.info("Deleting index: index={}", index);
        searchService.deleteIndex(index);
    }

    // ========== 向量搜索 ==========

    /**
     * 向量搜索
     *
     * @param index 索引名称
     * @param command 向量搜索命令
     * @param documentClass 文档类型
     * @param <T> 文档泛型
     * @return 向量搜索结果
     */
    public <T> VectorSearchResultDTO<T> vectorSearch(String index, VectorSearchCommand command, Class<T> documentClass) {
        log.info("Vector searching: index={}, vectorField={}, k={}", index, command.getVectorField(), command.getK());

        // 1. 转换命令对象为领域查询对象
        VectorSearchQuery query = convertToVectorSearchQuery(command);

        // 2. 调用领域服务执行向量搜索
        VectorSearchResult<T> result = searchService.vectorSearch(index, query, documentClass);

        // 3. 转换领域结果为DTO
        return convertToVectorSearchResultDTO(result);
    }

    /**
     * 创建向量索引
     *
     * @param index 索引名称
     * @param vectorField 向量字段名
     * @param dimension 向量维度
     * @param indexType 索引类型
     * @param distanceType 距离类型
     */
    public void createVectorIndex(String index, String vectorField, int dimension, String indexType, String distanceType) {
        log.info("Creating vector index: index={}, field={}, dimension={}, type={}, distance={}",
            index, vectorField, dimension, indexType, distanceType);
        searchService.createVectorIndex(index, vectorField, dimension,
            VectorIndexType.valueOf(indexType.toUpperCase()),
            VectorDistanceType.valueOf(distanceType.toUpperCase()));
    }

    // ========== AI搜索 ==========

    /**
     * AI增强搜索
     *
     * @param index 索引名称
     * @param command AI搜索命令
     * @param documentClass 文档类型
     * @param <T> 文档泛型
     * @return AI搜索结果
     */
    public <T> AiSearchResultDTO<T> aiSearch(String index, AiSearchCommand command, Class<T> documentClass) {
        log.info("AI searching: index={}, modelType={}, rerankStrategy={}",
            index, command.getModelType(), command.getRerankStrategy());

        // 1. 转换命令对象为领域查询对象
        AiSearchQuery query = convertToAiSearchQuery(command);

        // 2. 调用领域服务执行AI搜索
        AiSearchResult<T> result = searchService.aiSearch(index, query, documentClass);

        // 3. 转换领域结果为DTO
        return convertToAiSearchResultDTO(result);
    }

    // ========== 混合搜索 ==========

    /**
     * 混合搜索（BM25 + 向量）
     *
     * @param index 索引名称
     * @param command 混合搜索命令
     * @param documentClass 文档类型
     * @param <T> 文档泛型
     * @return 分页搜索结果
     */
    public <T> PageResultDTO<T> hybridSearch(String index, HybridSearchCommand command, Class<T> documentClass) {
        log.info("Hybrid searching: index={}, bm25Weight={}", index, command.getBm25Weight());

        // 1. 转换命令对象为领域查询对象
        HybridSearchQuery query = convertToHybridSearchQuery(command);

        // 2. 调用领域服务执行混合搜索
        SearchResult<T> searchResult = searchService.hybridSearch(index, query, documentClass);

        // 3. 转换领域结果为分页DTO
        return convertToPageResult(searchResult, command.getPageNo(), command.getPageSize());
    }

    // ========== 私有方法 ==========

    /**
     * 转换命令对象为领域查询对象
     */
    private SearchQuery convertToSearchQuery(SearchCommand command) {
        SearchQuery.SearchQueryBuilder builder = SearchQuery.builder()
            .keyword(command.getKeyword())
            .strategy(command.getStrategy())
            .from((command.getPageNo() - 1) * command.getPageSize())
            .size(command.getPageSize());

        // 转换过滤条件
        if (command.getFilters() != null) {
            List<SearchFilter> filters = command.getFilters().stream()
                .map(f -> SearchFilter.builder()
                    .field(f.getField())
                    .operator(FilterOperator.valueOf(f.getOperator()))
                    .value(f.getValue())
                    .build())
                .collect(Collectors.toList());
            builder.filters(filters);
        }

        // 转换排序条件
        if (command.getSorts() != null) {
            List<SearchSort> sorts = command.getSorts().stream()
                .map(s -> SearchSort.builder()
                    .field(s.getField())
                    .order(SortOrder.valueOf(s.getOrder()))
                    .build())
                .collect(Collectors.toList());
            builder.sorts(sorts);
        }

        // 转换聚合条件
        if (command.getAggregations() != null) {
            List<SearchAggregation> aggregations = command.getAggregations().stream()
                .map(a -> SearchAggregation.builder()
                    .name(a.getName())
                    .type(AggregationType.valueOf(a.getType()))
                    .field(a.getField())
                    .size(a.getSize() != null ? a.getSize() : 10)
                    .build())
                .collect(Collectors.toList());
            builder.aggregations(aggregations);
        }

        return builder.build();
    }

    /**
     * 转换领域结果为分页DTO
     */
    private <T> PageResultDTO<T> convertToPageResult(SearchResult<T> searchResult, int pageNo, int pageSize) {
        List<T> items = searchResult.getHits().stream()
            .map(SearchHit::getDocument)
            .collect(Collectors.toList());

        int totalPages = (int) Math.ceil((double) searchResult.getTotalHits() / pageSize);

        return PageResultDTO.<T>builder()
            .total(searchResult.getTotalHits())
            .pageNo(pageNo)
            .pageSize(pageSize)
            .totalPages(totalPages)
            .items(items)
            .build();
    }

    /**
     * 转换领域结果为DTO（包含聚合信息）
     */
    public <T> SearchResultDTO<T> convertToSearchResultDTO(SearchResult<T> searchResult) {
        List<SearchHitDTO<T>> hits = searchResult.getHits().stream()
            .map(hit -> SearchHitDTO.<T>builder()
                .id(hit.getId())
                .score(hit.getScore())
                .document(hit.getDocument())
                .build())
            .collect(Collectors.toList());

        List<SearchAggregationResultDTO> aggregations = searchResult.getAggregations().stream()
            .map(this::convertAggregationResult)
            .collect(Collectors.toList());

        return SearchResultDTO.<T>builder()
            .total(searchResult.getTotalHits())
            .maxScore(searchResult.getMaxScore())
            .hits(hits)
            .aggregations(aggregations)
            .took(searchResult.getTook())
            .build();
    }

    /**
     * 转换聚合结果
     */
    private SearchAggregationResultDTO convertAggregationResult(SearchAggregationResult result) {
        if (result.isHasValue()) {
            // 单值聚合
            return SearchAggregationResultDTO.builder()
                .name(result.getName())
                .value(result.getValue())
                .build();
        } else if (result.getBuckets() != null && !result.getBuckets().isEmpty()) {
            // 分桶聚合
            List<SearchAggregationResultDTO.AggregationBucketDTO> buckets = result.getBuckets().stream()
                .map(bucket -> SearchAggregationResultDTO.AggregationBucketDTO.builder()
                    .key(bucket.getKey())
                    .docCount(bucket.getDocCount())
                    .value(null) // AggregationBucket doesn't have single value
                    .build())
                .collect(Collectors.toList());

            return SearchAggregationResultDTO.builder()
                .name(result.getName())
                .buckets(buckets)
                .build();
        }

        return SearchAggregationResultDTO.builder()
            .name(result.getName())
            .build();
    }

    /**
     * 转换向量搜索命令为领域查询对象
     */
    private VectorSearchQuery convertToVectorSearchQuery(VectorSearchCommand command) {
        VectorSearchQuery.VectorSearchQueryBuilder builder = VectorSearchQuery.builder()
            .vector(command.getVector())
            .vectorField(command.getVectorField())
            .k(command.getK())
            .distanceType(VectorDistanceType.valueOf(command.getDistanceType().toUpperCase()));

        if (command.getIndexType() != null) {
            builder.indexType(VectorIndexType.valueOf(command.getIndexType().toUpperCase()));
        }

        // 转换过滤条件
        if (command.getFilters() != null) {
            List<SearchFilter> filters = command.getFilters().stream()
                .map(f -> SearchFilter.builder()
                    .field(f.getField())
                    .operator(FilterOperator.valueOf(f.getOperator()))
                    .value(f.getValue())
                    .build())
                .collect(Collectors.toList());
            builder.filters(filters);
        }

        return builder.build();
    }

    /**
     * 转换AI搜索命令为领域查询对象
     */
    private AiSearchQuery convertToAiSearchQuery(AiSearchCommand command) {
        AiSearchQuery.AiSearchQueryBuilder builder = AiSearchQuery.builder()
            .queryText(command.getQueryText())
            .modelType(AiSearchModelType.valueOf(command.getModelType().toUpperCase()))
            .rerankStrategy(RerankStrategyType.valueOf(command.getRerankStrategy().toUpperCase()))
            .bm25Weight(command.getBm25Weight())
            .size(command.getSize())
            .from(command.getFrom())
            .enableQueryExpansion(command.getEnableQueryExpansion());

        // 转换过滤条件
        if (command.getFilters() != null) {
            List<SearchFilter> filters = command.getFilters().stream()
                .map(f -> SearchFilter.builder()
                    .field(f.getField())
                    .operator(FilterOperator.valueOf(f.getOperator()))
                    .value(f.getValue())
                    .build())
                .collect(Collectors.toList());
            builder.filters(filters);
        }

        return builder.build();
    }

    /**
     * 转换混合搜索命令为领域查询对象
     */
    private HybridSearchQuery convertToHybridSearchQuery(HybridSearchCommand command) {
        return HybridSearchQuery.builder()
            .queryText(command.getQueryText())
            .queryVector(command.getQueryVector())
            .vectorField(command.getVectorField())
            .k(command.getK())
            .distanceType(VectorDistanceType.valueOf(command.getDistanceType().toUpperCase()))
            .bm25Weight(command.getBm25Weight())
            .size(command.getSize())
            .from(command.getFrom())
            .build();
    }

    /**
     * 转换向量搜索结果为DTO
     */
    private <T> VectorSearchResultDTO<T> convertToVectorSearchResultDTO(VectorSearchResult<T> result) {
        List<VectorSearchResultDTO.VectorSearchHitDTO<T>> hits = result.getHits().stream()
            .map(hit -> VectorSearchResultDTO.VectorSearchHitDTO.<T>builder()
                .id(hit.getId())
                .score(hit.getScore())
                .document(hit.getDocument())
                .build())
            .collect(Collectors.toList());

        return VectorSearchResultDTO.<T>builder()
            .hits(hits)
            .took(result.getTook())
            .build();
    }

    /**
     * 转换AI搜索结果为DTO
     */
    private <T> AiSearchResultDTO<T> convertToAiSearchResultDTO(AiSearchResult<T> result) {
        List<AiSearchResultDTO.AiSearchHitDTO<T>> hits = result.getHits().stream()
            .map(hit -> AiSearchResultDTO.AiSearchHitDTO.<T>builder()
                .id(hit.getId())
                .score(hit.getScore())
                .bm25Score(hit.getBm25Score())
                .aiScore(hit.getAiScore())
                .rankChange(hit.getRankChange())
                .document(hit.getDocument())
                .extraInfo(hit.getExtraInfo())
                .build())
            .collect(Collectors.toList());

        return AiSearchResultDTO.<T>builder()
            .hits(hits)
            .totalHits(result.getTotalHits())
            .took(result.getTook())
            .rerankStrategy(null) // AiSearchResult doesn't have rerankStrategy field
            .expandedTerms(result.getExpandedTerms())
            .build();
    }
}
