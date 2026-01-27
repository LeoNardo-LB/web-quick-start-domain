package cases.unittest;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.smm.archetype.domain.bizshared.client.SearchClient;
import org.smm.archetype.domain.common.search.enums.FilterOperator;
import org.smm.archetype.domain.common.search.enums.SortOrder;
import org.smm.archetype.domain.common.search.query.SearchFilter;
import org.smm.archetype.domain.common.search.query.SearchQuery;
import org.smm.archetype.domain.common.search.query.SearchSort;
import org.smm.archetype.domain.common.search.enums.RerankStrategyType;
import org.smm.archetype.domain.common.search.enums.VectorDistanceType;
import org.smm.archetype.domain.common.search.enums.VectorIndexType;
import org.smm.archetype.domain.common.search.query.AiSearchQuery;
import org.smm.archetype.domain.common.search.query.VectorSearchQuery;
import org.smm.archetype.domain.common.search.query.HybridSearchQuery;
import org.smm.archetype.domain.common.search.result.SearchResult;
import org.smm.archetype.domain.common.search.result.VectorSearchResult;
import org.smm.archetype.domain.common.search.result.AiSearchResult;
import org.smm.archetype.infrastructure.common.search.SearchServiceImpl;
import support.UnitTestBase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SearchServiceImpl单元测试
 *
 * @author Leonardo
 * @since 2026-01-14
 */
@DisplayName("SearchServiceImpl单元测试")
class SearchServiceImplUTest extends UnitTestBase {

    /**
     * 测试文档类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class TestDocument {
        private String name;
        private Double price;
    }

    @Mock
    private SearchClient searchClient;

    private SearchServiceImpl searchService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void setUpMocks() {
        searchService = new SearchServiceImpl(searchClient, objectMapper);
    }

    // ========== search测试 ==========

    @Test
    @DisplayName("搜索文档 - 关键词查询 - 返回结果列表")
    void search_WithKeyword_ReturnsResults() {
        // Arrange
        String index = "products";
        SearchQuery query = SearchQuery.builder()
            .keyword("iPhone")
            .from(0)
            .size(10)
            .build();

        Map<String, Object> mockResponse = buildMockSearchResponse(1);
        when(searchClient.search(anyString(), anyString(), anyInt(), anyInt()))
            .thenReturn(mockResponse);

        // Act
        SearchResult<TestDocument> result;
        try {
            result = searchService.search(index, query, TestDocument.class);
        } catch (Exception e) {
            System.err.println("Exception caught: " + e.getClass().getName());
            System.err.println("Message: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Cause: " + e.getCause().getClass().getName() + ": " + e.getCause().getMessage());
                e.getCause().printStackTrace();
            }
            throw e;
        }

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTotalHits()).isEqualTo(1);
        assertThat(result.getHits()).hasSize(1);
        assertThat(result.getHits().get(0).getId()).isEqualTo("P001");

        verify(searchClient, times(1)).search(eq(index), anyString(), eq(0), eq(10));
    }

    @Test
    @DisplayName("搜索文档 - 带过滤条件 - 返回过滤后的结果")
    void search_WithFilters_ReturnsFilteredResults() {
        // Arrange
        String index = "products";
        SearchQuery query = SearchQuery.builder()
            .keyword("手机")
            .filters(List.of(
                SearchFilter.builder().field("category").operator(FilterOperator.EQ).value("electronics").build(),
                SearchFilter.builder().field("price").operator(FilterOperator.GTE).value(1000).build()
            ))
            .from(0)
            .size(10)
            .build();

        Map<String, Object> mockResponse = buildMockSearchResponse(2);
        when(searchClient.search(eq(index), anyString(), eq(0), eq(10)))
            .thenReturn(mockResponse);

        // Act
        SearchResult<TestDocument> result = searchService.search(index, query, TestDocument.class);

        // Assert
        assertThat(result.getTotalHits()).isEqualTo(2);
        verify(searchClient, times(1)).search(eq(index), anyString(), eq(0), eq(10));
    }

    @Test
    @DisplayName("搜索文档 - 带排序 - 返回排序后的结果")
    void search_WithSort_ReturnsSortedResults() {
        // Arrange
        String index = "products";
        SearchQuery query = SearchQuery.builder()
            .keyword("手机")
            .sorts(List.of(
                SearchSort.builder().field("price").order(SortOrder.DESC).build(),
                SearchSort.builder().field("name").order(SortOrder.ASC).build()
            ))
            .from(0)
            .size(10)
            .build();

        Map<String, Object> mockResponse = buildMockSearchResponse(3);
        when(searchClient.search(eq(index), anyString(), eq(0), eq(10)))
            .thenReturn(mockResponse);

        // Act
        SearchResult<TestDocument> result = searchService.search(index, query, TestDocument.class);

        // Assert
        assertThat(result.getTotalHits()).isEqualTo(3);
        verify(searchClient, times(1)).search(eq(index), anyString(), eq(0), eq(10));
    }

    @Test
    @DisplayName("搜索文档 - match_all查询 - 返回所有文档")
    void search_MatchAll_ReturnsAllDocuments() {
        // Arrange
        String index = "products";
        SearchQuery query = SearchQuery.builder()
            .from(0)
            .size(10)
            .build();

        Map<String, Object> mockResponse = buildMockSearchResponse(5);
        when(searchClient.search(eq(index), anyString(), eq(0), eq(10)))
            .thenReturn(mockResponse);

        // Act
        SearchResult<TestDocument> result = searchService.search(index, query, TestDocument.class);

        // Assert
        assertThat(result.getTotalHits()).isEqualTo(5);
        verify(searchClient, times(1)).search(eq(index), anyString(), eq(0), eq(10));
    }

    // ========== index测试 ==========

    @Test
    @DisplayName("索引文档 - 单个文档 - 成功索引")
    void index_SingleDocument_Success() {
        // Arrange
        String index = "products";
        String id = "P001";
        TestDocument document = new TestDocument("iPhone 15", 799.99);

        // Act
        searchService.index(index, id, document);

        // Assert
        verify(searchClient, times(1)).index(eq(index), eq(id), any(Map.class));
    }

    // ========== bulkIndex测试 ==========

    @Test
    @DisplayName("批量索引 - 多个文档 - 成功索引")
    void bulkIndex_MultipleDocuments_Success() {
        // Arrange
        String index = "products";
        Map<String, TestDocument> documents = new HashMap<>();

        TestDocument doc1 = new TestDocument("iPhone 15", 799.99);
        documents.put("P001", doc1);

        TestDocument doc2 = new TestDocument("Samsung Galaxy", 699.99);
        documents.put("P002", doc2);

        // Act
        searchService.bulkIndex(index, documents);

        // Assert
        verify(searchClient, times(1)).bulkIndex(eq(index), any());
    }

    // ========== delete测试 ==========

    @Test
    @DisplayName("删除文档 - 指定ID - 成功删除")
    void delete_ById_Success() {
        // Arrange
        String index = "products";
        String id = "P001";

        // Act
        searchService.delete(index, id);

        // Assert
        verify(searchClient, times(1)).delete(eq(index), eq(id));
    }

    // ========== bulkDelete测试 ==========

    @Test
    @DisplayName("批量删除 - 多个ID - 成功删除")
    void bulkDelete_MultipleIds_Success() {
        // Arrange
        String index = "products";
        List<String> ids = List.of("P001", "P002", "P003");

        // Act
        searchService.bulkDelete(index, ids);

        // Assert
        verify(searchClient, times(1)).bulkDelete(eq(index), eq(ids));
    }

    // ========== get测试 ==========

    @Test
    @DisplayName("获取文档 - 存在的ID - 返回文档")
    void get_ExistingId_ReturnsDocument() {
        // Arrange
        String index = "products";
        String id = "P001";
        Map<String, Object> source = new HashMap<>();
        source.put("name", "iPhone 15");
        source.put("price", 799.99);

        when(searchClient.get(index, id)).thenReturn(source);

        // Act
        TestDocument result = searchService.get(index, id, TestDocument.class);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("iPhone 15");
        assertThat(result.getPrice()).isEqualTo(799.99);
        verify(searchClient, times(1)).get(eq(index), eq(id));
    }

    @Test
    @DisplayName("获取文档 - 不存在的ID - 返回null")
    void get_NonExistingId_ReturnsNull() {
        // Arrange
        String index = "products";
        String id = "P999";

        when(searchClient.get(index, id)).thenReturn(null);

        // Act
        TestDocument result = searchService.get(index, id, TestDocument.class);

        // Assert
        assertThat(result).isNull();
        verify(searchClient, times(1)).get(eq(index), eq(id));
    }

    // ========== bulkGet测试 ==========

    @Test
    @DisplayName("批量获取 - 多个ID - 返回文档Map")
    void bulkGet_MultipleIds_ReturnsDocumentMap() {
        // Arrange
        String index = "products";
        List<String> ids = List.of("P001", "P002");

        Map<String, Map<String, Object>> mockResults = new HashMap<>();
        Map<String, Object> doc1 = new HashMap<>();
        doc1.put("name", "iPhone 15");
        doc1.put("price", 799.99);
        mockResults.put("P001", doc1);

        Map<String, Object> doc2 = new HashMap<>();
        doc2.put("name", "Samsung Galaxy");
        doc2.put("price", 699.99);
        mockResults.put("P002", doc2);

        when(searchClient.bulkGet(index, ids)).thenReturn(mockResults);

        // Act
        Map<String, TestDocument> result = searchService.bulkGet(index, ids, TestDocument.class);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get("P001").getName()).isEqualTo("iPhone 15");
        assertThat(result.get("P002").getName()).isEqualTo("Samsung Galaxy");
        verify(searchClient, times(1)).bulkGet(eq(index), eq(ids));
    }

    // ========== refresh测试 ==========

    @Test
    @DisplayName("刷新索引 - 指定索引 - 成功刷新")
    void refresh_ByIndex_Success() {
        // Arrange
        String index = "products";

        // Act
        searchService.refresh(index);

        // Assert
        verify(searchClient, times(1)).refresh(eq(index));
    }

    // ========== existsIndex测试 ==========

    @Test
    @DisplayName("检查索引存在 - 存在的索引 - 返回true")
    void existsIndex_ExistingIndex_ReturnsTrue() {
        // Arrange
        String index = "products";
        when(searchClient.existsIndex(index)).thenReturn(true);

        // Act
        boolean result = searchService.existsIndex(index);

        // Assert
        assertThat(result).isTrue();
        verify(searchClient, times(1)).existsIndex(eq(index));
    }

    @Test
    @DisplayName("检查索引存在 - 不存在的索引 - 返回false")
    void existsIndex_NonExistingIndex_ReturnsFalse() {
        // Arrange
        String index = "products";
        when(searchClient.existsIndex(index)).thenReturn(false);

        // Act
        boolean result = searchService.existsIndex(index);

        // Assert
        assertThat(result).isFalse();
        verify(searchClient, times(1)).existsIndex(eq(index));
    }

    // ========== createIndex测试 ==========

    @Test
    @DisplayName("创建索引 - 指定映射 - 成功创建")
    void createIndex_WithMapping_Success() {
        // Arrange
        String index = "products";
        String mapping = "{\"mappings\":{}}";

        // Act
        searchService.createIndex(index, mapping);

        // Assert
        verify(searchClient, times(1)).createIndex(eq(index), eq(mapping));
    }

    // ========== deleteIndex测试 ==========

    @Test
    @DisplayName("删除索引 - 指定索引 - 成功删除")
    void deleteIndex_ByIndex_Success() {
        // Arrange
        String index = "products";

        // Act
        searchService.deleteIndex(index);

        // Assert
        verify(searchClient, times(1)).deleteIndex(eq(index));
    }

    // ========== vectorSearch测试 ==========

    @Test
    @DisplayName("向量搜索 - 正常查询 - 返回向量搜索结果")
    void vectorSearch_ValidQuery_ReturnsVectorSearchResults() {
        // Arrange
        String index = "products";
        List<Float> queryVector = List.of(0.1f, 0.2f, 0.3f);
        VectorSearchQuery query = VectorSearchQuery.builder()
            .vector(queryVector)
            .vectorField("embedding")
            .k(10)
            .distanceType(VectorDistanceType.COSINE)
            .build();

        Map<String, Object> mockResponse = buildMockVectorSearchResponse(2);
        when(searchClient.vectorSearch(eq(index), anyString())).thenReturn(mockResponse);

        // Act
        VectorSearchResult<TestDocument> result = searchService.vectorSearch(index, query, TestDocument.class);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getHits()).hasSize(2);
        verify(searchClient, times(1)).vectorSearch(eq(index), anyString());
    }

    // ========== aiSearch测试 ==========

    @Test
    @DisplayName("AI搜索 - 无重排序 - 返回BM25结果")
    void aiSearch_WithoutReranking_ReturnsBm25Results() {
        // Arrange
        String index = "products";
        AiSearchQuery query = AiSearchQuery.builder()
            .queryText("iPhone")
            .rerankStrategy(RerankStrategyType.NONE)
            .filters(List.of())
            .size(10)
            .build();

        Map<String, Object> mockResponse = buildMockSearchResponse(3);
        when(searchClient.search(eq(index), anyString(), anyInt(), anyInt())).thenReturn(mockResponse);

        // Act
        AiSearchResult<TestDocument> result = searchService.aiSearch(index, query, TestDocument.class);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTotalHits()).isEqualTo(3);
        verify(searchClient, times(1)).search(eq(index), anyString(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("AI搜索 - SCORE_WEIGHTED重排序 - 返回加权结果")
    void aiSearch_WithScoreWeightedReranking_ReturnsWeightedResults() {
        // Arrange
        String index = "products";
        AiSearchQuery query = AiSearchQuery.builder()
            .queryText("iPhone")
            .rerankStrategy(RerankStrategyType.SCORE_WEIGHTED)
            .bm25Weight(0.7f)
            .filters(List.of())
            .size(10)
            .build();

        Map<String, Object> mockResponse = buildMockSearchResponse(3);
        when(searchClient.search(eq(index), anyString(), anyInt(), anyInt())).thenReturn(mockResponse);

        // Act
        AiSearchResult<TestDocument> result = searchService.aiSearch(index, query, TestDocument.class);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTotalHits()).isEqualTo(3);
        verify(searchClient, times(1)).search(eq(index), anyString(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("AI搜索 - RRF重排序 - 返回RRF结果")
    void aiSearch_WithRRFReranking_ReturnsRRFResults() {
        // Arrange
        String index = "products";
        AiSearchQuery query = AiSearchQuery.builder()
            .queryText("iPhone")
            .rerankStrategy(RerankStrategyType.RRF)
            .filters(List.of())
            .size(10)
            .build();

        Map<String, Object> mockResponse = buildMockSearchResponse(3);
        when(searchClient.search(eq(index), anyString(), anyInt(), anyInt())).thenReturn(mockResponse);

        // Act
        AiSearchResult<TestDocument> result = searchService.aiSearch(index, query, TestDocument.class);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTotalHits()).isEqualTo(3);
        verify(searchClient, times(1)).search(eq(index), anyString(), anyInt(), anyInt());
    }

    // ========== hybridSearch测试 ==========

    @Test
    @DisplayName("混合搜索 - BM25+向量融合 - 返回融合结果")
    void hybridSearch_Bm25AndVectorFusion_ReturnsFusedResults() {
        // Arrange
        String index = "products";
        List<Float> queryVector = List.of(0.1f, 0.2f, 0.3f);
        HybridSearchQuery query = HybridSearchQuery.builder()
            .queryText("iPhone")
            .queryVector(queryVector)
            .vectorField("embedding")
            .k(10)
            .distanceType(VectorDistanceType.COSINE)
            .bm25Weight(0.5f)
            .size(10)
            .from(0)
            .build();

        Map<String, Object> mockResponse = buildMockSearchResponse(3);
        when(searchClient.search(eq(index), anyString(), anyInt(), anyInt())).thenReturn(mockResponse);

        // Act
        SearchResult<TestDocument> result = searchService.hybridSearch(index, query, TestDocument.class);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTotalHits()).isEqualTo(3);
        verify(searchClient, times(1)).search(eq(index), anyString(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("混合搜索 - BM25权重1.0 - 仅返回BM25结果")
    void hybridSearch_Bm25WeightOne_ReturnsOnlyBm25Results() {
        // Arrange
        String index = "products";
        List<Float> queryVector = List.of(0.1f, 0.2f, 0.3f);
        HybridSearchQuery query = HybridSearchQuery.builder()
            .queryText("iPhone")
            .queryVector(queryVector)
            .vectorField("embedding")
            .k(10)
            .distanceType(VectorDistanceType.COSINE)
            .bm25Weight(1.0f)
            .size(10)
            .from(0)
            .build();

        Map<String, Object> mockResponse = buildMockSearchResponse(3);
        when(searchClient.search(eq(index), anyString(), anyInt(), anyInt())).thenReturn(mockResponse);

        // Act
        SearchResult<TestDocument> result = searchService.hybridSearch(index, query, TestDocument.class);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTotalHits()).isEqualTo(3);
        verify(searchClient, times(1)).search(eq(index), anyString(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("混合搜索 - BM25权重0.0 - 仅返回向量结果")
    void hybridSearch_Bm25WeightZero_ReturnsOnlyVectorResults() {
        // Arrange
        String index = "products";
        List<Float> queryVector = List.of(0.1f, 0.2f, 0.3f);
        HybridSearchQuery query = HybridSearchQuery.builder()
            .queryText("iPhone")
            .queryVector(queryVector)
            .vectorField("embedding")
            .k(10)
            .distanceType(VectorDistanceType.COSINE)
            .bm25Weight(0.0f)
            .size(10)
            .from(0)
            .build();

        Map<String, Object> mockResponse = buildMockSearchResponse(2);
        when(searchClient.search(eq(index), anyString(), anyInt(), anyInt())).thenReturn(mockResponse);

        // Act
        SearchResult<TestDocument> result = searchService.hybridSearch(index, query, TestDocument.class);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTotalHits()).isEqualTo(2);
        verify(searchClient, times(1)).search(eq(index), anyString(), anyInt(), anyInt());
    }

    // ========== createVectorIndex测试 ==========

    @Test
    @DisplayName("创建向量索引 - 指定维度和类型 - 成功创建")
    void createVectorIndex_WithDimensionAndType_Success() {
        // Arrange
        String index = "products";
        String vectorField = "embedding";
        int dimension = 1536;
        VectorIndexType indexType = VectorIndexType.HNSW;
        VectorDistanceType distanceType = VectorDistanceType.COSINE;

        // Act
        searchService.createVectorIndex(index, vectorField, dimension, indexType, distanceType);

        // Assert
        verify(searchClient, times(1)).createVectorIndex(eq(index), eq(vectorField), eq(dimension),
            eq("hnsw"), eq("cosine"));
    }

    // ========== 辅助方法 ==========

    /**
     * 构建模拟搜索响应
     */
    private Map<String, Object> buildMockSearchResponse(int count) {
        Map<String, Object> hit1 = new HashMap<>();
        hit1.put("_id", "P001");
        hit1.put("_score", 1.0f);
        Map<String, Object> source1 = new HashMap<>();
        source1.put("name", "iPhone 15");
        source1.put("price", 799.99);
        hit1.put("_source", source1);

        Map<String, Object> hit2 = new HashMap<>();
        hit2.put("_id", "P002");
        hit2.put("_score", 0.9f);
        Map<String, Object> source2 = new HashMap<>();
        source2.put("name", "Samsung Galaxy");
        source2.put("price", 699.99);
        hit2.put("_source", source2);

        Map<String, Object> hit3 = new HashMap<>();
        hit3.put("_id", "P003");
        hit3.put("_score", 0.8f);
        Map<String, Object> source3 = new HashMap<>();
        source3.put("name", "Google Pixel");
        source3.put("price", 599.99);
        hit3.put("_source", source3);

        List<Map<String, Object>> hitsList = List.of(hit1, hit2, hit3).subList(0, Math.min(count, 3));

        Map<String, Object> total = Map.of("value", count, "relation", "eq");
        Map<String, Object> hits = Map.of(
            "total", total,
            "max_score", 1.0f,
            "hits", hitsList
        );

        return Map.of(
            "took", 5L,
            "hits", hits
        );
    }

    /**
     * 构建模拟向量搜索响应
     */
    private Map<String, Object> buildMockVectorSearchResponse(int count) {
        Map<String, Object> hit1 = new HashMap<>();
        hit1.put("_id", "P001");
        hit1.put("_score", 0.95);
        Map<String, Object> source1 = new HashMap<>();
        source1.put("name", "iPhone 15");
        source1.put("price", 799.99);
        hit1.put("_source", source1);

        Map<String, Object> hit2 = new HashMap<>();
        hit2.put("_id", "P002");
        hit2.put("_score", 0.85);
        Map<String, Object> source2 = new HashMap<>();
        source2.put("name", "Samsung Galaxy");
        source2.put("price", 699.99);
        hit2.put("_source", source2);

        Map<String, Object> hit3 = new HashMap<>();
        hit3.put("_id", "P003");
        hit3.put("_score", 0.75);
        Map<String, Object> source3 = new HashMap<>();
        source3.put("name", "Google Pixel");
        source3.put("price", 599.99);
        hit3.put("_source", source3);

        List<Map<String, Object>> hitsList = List.of(hit1, hit2, hit3).subList(0, Math.min(count, 3));

        Map<String, Object> total = Map.of("value", count, "relation", "eq");
        Map<String, Object> hits = Map.of(
            "total", total,
            "max_score", 0.95,
            "hits", hitsList
        );

        return Map.of(
            "took", 3L,
            "hits", hits
        );
    }
}
