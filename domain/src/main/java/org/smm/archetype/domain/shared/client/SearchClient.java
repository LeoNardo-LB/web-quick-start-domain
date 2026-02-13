package org.smm.archetype.domain.shared.client;

import java.util.List;
import java.util.Map;

/**
 * 搜索技术客户端接口，提供ES底层操作。
 */
public interface SearchClient {

    /**
     * 索引文档
     * @param index 索引名称
     * @param id 文档ID
     * @param document 文档内容(Map形式)
     */
    void index(String index, String id, Map<String, Object> document);

    /**
     * 批量索引
     * @param index 索引名称
     * @param documents 文档列表(id, document)
     */
    void bulkIndex(String index, List<Map.Entry<String, Map<String, Object>>> documents);

    /**
     * 删除文档
     * @param index 索引名称
     * @param id 文档ID
     */
    void delete(String index, String id);

    /**
     * 批量删除
     * @param index 索引名称
     * @param ids 文档ID列表
     */
    void bulkDelete(String index, List<String> ids);

    /**
     * 获取文档
     * @param index 索引名称
     * @param id 文档ID
     * @return 文档内容，不存在返回null
     */
    Map<String, Object> get(String index, String id);

    /**
     * 批量获取
     * @param index 索引名称
     * @param ids 文档ID列表
     * @return 文档内容Map
     */
    Map<String, Map<String, Object>> bulkGet(String index, List<String> ids);

    /**
     * 搜索文档
     * @param index 索引名称
     * @param query 查询条件(JSON字符串)
     * @return 搜索结果(Map形式)
     */
    Map<String, Object> search(String index, String query);

    /**
     * 搜索文档（带分页）
     * @param index 索引名称
     * @param query 查询条件(JSON字符串)
     * @param from 从第几条开始
     * @param size 返回多少条
     * @return 搜索结果(Map形式)
     */
    Map<String, Object> search(String index, String query, int from, int size);

    /**
     * 聚合查询
     * @param index 索引名称
     * @param query 聚合查询条件(JSON字符串)
     * @return 聚合结果(Map形式)
     */
    Map<String, Object> aggregate(String index, String query);

    /**
     * 判断索引是否存在
     * @param index 索引名称
     * @return 是否存在
     */
    boolean existsIndex(String index);

    /**
     * 创建索引
     * @param index 索引名称
     * @param mapping 索引映射(JSON字符串)
     */
    void createIndex(String index, String mapping);

    /**
     * 删除索引
     * @param index 索引名称
     */
    void deleteIndex(String index);

    /**
     * 刷新索引（使文档立即可搜索）
     * @param index 索引名称
     */
    void refresh(String index);

    /**
     * 向量搜索
     *
     * @param index 索引名称
     * @param query 向量搜索查询条件(JSON字符串)
     * @return 向量搜索结果(Map形式)
     */
    Map<String, Object> vectorSearch(String index, String query);

    /**
     * 创建向量索引
     *
     * @param index 索引名称
     * @param vectorField 向量字段名
     * @param dimension 向量维度
     * @param indexType 索引类型(JSON字符串，如 "hnsw", "ivf", "flat")
     * @param distanceType 距离类型(JSON字符串，如 "cosine", "l2", "dot_product")
     */
    void createVectorIndex(String index, String vectorField, int dimension, String indexType, String distanceType);

    /**
     * 批量向量搜索
     *
     * @param index 索引名称
     * @param queries 批量向量搜索查询列表(JSON字符串数组)
     * @return 批量向量搜索结果列表(Map形式)
     */
    List<Map<String, Object>> bulkVectorSearch(String index, List<String> queries);
}
