package org.smm.archetype.domain.platform.search;

import org.smm.archetype.domain.platform.search.query.SearchQuery;
import org.smm.archetype.domain.platform.search.query.VectorSearchQuery;
import org.smm.archetype.domain.platform.search.result.SearchResult;
import org.smm.archetype.domain.platform.search.result.VectorSearchResult;
import org.smm.archetype.domain.platform.search.enums.VectorIndexType;
import org.smm.archetype.domain.platform.search.enums.VectorDistanceType;

import java.util.List;
import java.util.Map;

/**
 * 搜索服务接口，提供业务层的搜索能力。
 */
public interface SearchService {

    /**
     * 搜索文档
     *
     * @param index 索引名称
     * @param query 搜索查询对象
     * @param documentClass 文档类型
     * @param <T> 文档泛型
     * @return 搜索结果
     */
    <T> SearchResult<T> search(String index, SearchQuery query, Class<T> documentClass);

    /**
     * 索引单个文档
     *
     * @param index 索引名称
     * @param id 文档ID
     * @param document 文档对象
     * @param <T> 文档泛型
     */
    <T> void index(String index, String id, T document);

    /**
     * 批量索引文档
     *
     * @param index 索引名称
     * @param documents 文档Map（ID -> 文档对象）
     * @param <T> 文档泛型
     */
    <T> void bulkIndex(String index, Map<String, T> documents);

    /**
     * 删除文档
     *
     * @param index 索引名称
     * @param id 文档ID
     */
    void delete(String index, String id);

    /**
     * 批量删除文档
     *
     * @param index 索引名称
     * @param ids 文档ID列表
     */
    void bulkDelete(String index, List<String> ids);

    /**
     * 获取单个文档
     *
     * @param index 索引名称
     * @param id 文档ID
     * @param documentClass 文档类型
     * @param <T> 文档泛型
     * @return 文档对象，不存在时返回null
     */
    <T> T get(String index, String id, Class<T> documentClass);

    /**
     * 批量获取文档
     *
     * @param index 索引名称
     * @param ids 文档ID列表
     * @param documentClass 文档类型
     * @param <T> 文档泛型
     * @return 文档Map（ID -> 文档对象）
     */
    <T> Map<String, T> bulkGet(String index, List<String> ids, Class<T> documentClass);

    /**
     * 刷新索引
     *
     * @param index 索引名称
     */
    void refresh(String index);

    /**
     * 检查索引是否存在
     *
     * @param index 索引名称
     * @return 是否存在
     */
    boolean existsIndex(String index);

    /**
     * 创建索引
     *
     * @param index 索引名称
     * @param mapping 索引映射（JSON格式）
     */
    void createIndex(String index, String mapping);

    /**
     * 删除索引
     *
     * @param index 索引名称
     */
    void deleteIndex(String index);

    /**
     * 向量搜索
     *
     * @param index 索引名称
     * @param query 向量搜索查询对象
     * @param documentClass 文档类型
     * @param <T> 文档泛型
     * @return 向量搜索结果
     */
    <T> VectorSearchResult<T> vectorSearch(String index, VectorSearchQuery query, Class<T> documentClass);

    /**
     * 创建向量索引
     *
     * @param index 索引名称
     * @param vectorField 向量字段名
     * @param dimension 向量维度
     * @param indexType 索引类型
     * @param distanceType 距离类型
     */
    void createVectorIndex(String index, String vectorField, int dimension,
                          VectorIndexType indexType, VectorDistanceType distanceType);

    /**
     * AI增强搜索
     *
     * @param index 索引名称
     * @param query AI搜索查询对象
     * @param documentClass 文档类型
     * @param <T> 文档泛型
     * @return AI搜索结果
     */
    <T> org.smm.archetype.domain.platform.search.result.AiSearchResult<T> aiSearch(
        String index,
        org.smm.archetype.domain.platform.search.query.AiSearchQuery query,
        Class<T> documentClass
    );

    /**
     * 混合搜索（BM25 + 向量）
     *
     * @param index 索引名称
     * @param query 混合搜索查询对象
     * @param documentClass 文档类型
     * @param <T> 文档泛型
     * @return 搜索结果
     */
    <T> SearchResult<T> hybridSearch(
        String index,
        org.smm.archetype.domain.platform.search.query.HybridSearchQuery query,
        Class<T> documentClass
    );
}
