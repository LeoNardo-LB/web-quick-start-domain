package cases.unittest.org.smm.archetype.domain.common.search.result;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.smm.archetype.domain.common.search.result.AggregationBucket;
import org.smm.archetype.domain.common.search.result.SearchAggregationResult;
import org.smm.archetype.domain.common.search.result.SearchHit;
import org.smm.archetype.domain.common.search.result.SearchResult;
import support.UnitTestBase;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SearchResult单元测试。
 */
@DisplayName("SearchResult单元测试")
class SearchResultUTest extends UnitTestBase {

    @Test
    @DisplayName("构建搜索结果 - 完整参数 - 成功构建")
    void build_FullParams_Success() {
        // Arrange
        List<SearchHit<String>> hits = List.of(
            SearchHit.<String>builder()
                .id("P001")
                .score(1.5f)
                .document("iPhone 15")
                .build()
        );

        // Act
        SearchResult<String> result = SearchResult.<String>builder()
            .totalHits(1)
            .maxScore(1.5f)
            .hits(hits)
            .took(50L)
            .build();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTotalHits()).isEqualTo(1);
        assertThat(result.getMaxScore()).isEqualTo(1.5f);
        assertThat(result.getHits()).hasSize(1);
        assertThat(result.getTook()).isEqualTo(50L);
    }

    @Test
    @DisplayName("构建搜索结果 - 带聚合 - 包含聚合结果")
    void build_WithAggregations_ContainsAggregations() {
        // Arrange
        List<SearchAggregationResult> aggregations = List.of(
            SearchAggregationResult.ofSingleValue("avg_price", 799.99),
            SearchAggregationResult.ofBuckets("by_category", List.of(
                AggregationBucket.builder().key("electronics").docCount(100).build()
            ))
        );

        // Act
        SearchResult<String> result = SearchResult.<String>builder()
            .totalHits(1)
            .hits(List.of())
            .aggregations(aggregations)
            .build();

        // Assert
        assertThat(result.getAggregations()).hasSize(2);
        assertThat(result.getAggregations().get(0).getName()).isEqualTo("avg_price");
        assertThat(result.getAggregations().get(1).getName()).isEqualTo("by_category");
    }

    @Test
    @DisplayName("使用默认值 - 不设置参数 - 使用默认值")
    void build_NoParams_UsesDefaults() {
        // Arrange & Act
        SearchResult<String> result = SearchResult.<String>builder().build();

        // Assert
        assertThat(result.getTotalHits()).isEqualTo(0);
        assertThat(result.getMaxScore()).isEqualTo(0.0f);
        assertThat(result.getHits()).isNotNull();
        assertThat(result.getHits()).isEmpty();
        assertThat(result.getAggregations()).isNotNull();
        assertThat(result.getAggregations()).isEmpty();
    }

    @Test
    @DisplayName("泛型支持 - 不同文档类型 - 支持泛型")
    void build_GenericType_SupportsGenericType() {
        // Arrange
        class Product {
            String name;
            double price;
        }

        Product product = new Product();
        product.name = "iPhone 15";
        product.price = 799.99;

        List<SearchHit<Product>> hits = List.of(
            SearchHit.<Product>builder()
                .id("P001")
                .score(1.0f)
                .document(product)
                .build()
        );

        // Act
        SearchResult<Product> result = SearchResult.<Product>builder()
            .totalHits(1)
            .hits(hits)
            .build();

        // Assert
        assertThat(result.getHits()).hasSize(1);
        assertThat(result.getHits().get(0).getDocument().name).isEqualTo("iPhone 15");
        assertThat(result.getHits().get(0).getDocument().price).isEqualTo(799.99);
    }

    @Test
    @DisplayName("构建空结果 - 没有命中 - 返回空结果")
    void build_EmptyResult_NoHits_ReturnsEmptyResult() {
        // Arrange & Act
        SearchResult<String> result = SearchResult.<String>builder()
            .totalHits(0)
            .hits(List.of())
            .build();

        // Assert
        assertThat(result.getTotalHits()).isEqualTo(0);
        assertThat(result.getHits()).isEmpty();
    }

    @Test
    @DisplayName("使用@NoArgsConstructor - 创建空对象 - 成功创建")
    void noArgsConstructor_CreateEmpty_Success() {
        // Arrange & Act
        SearchResult<String> result = new SearchResult<>();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTotalHits()).isEqualTo(0);
        assertThat(result.getMaxScore()).isEqualTo(0.0f);
        assertThat(result.getHits()).isNotNull(); // 默认空List
        assertThat(result.getAggregations()).isNotNull(); // 默认空List
    }
}
