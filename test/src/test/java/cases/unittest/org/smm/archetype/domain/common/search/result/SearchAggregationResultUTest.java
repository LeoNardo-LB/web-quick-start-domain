package cases.unittest.org.smm.archetype.domain.common.search.result;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.smm.archetype.domain.common.search.result.AggregationBucket;
import org.smm.archetype.domain.common.search.result.SearchAggregationResult;
import support.UnitTestBase;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SearchAggregationResult单元测试。
 */
@DisplayName("SearchAggregationResult单元测试")
class SearchAggregationResultUTest extends UnitTestBase {

    @Test
    @DisplayName("创建单值聚合 - ofSingleValue方法 - 返回单值结果")
    void ofSingleValue_ValidParams_ReturnsSingleValueResult() {
        // Arrange & Act
        SearchAggregationResult result = SearchAggregationResult.ofSingleValue("avg_price", 799.99);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("avg_price");
        assertThat(result.getValue()).isEqualTo(799.99);
        assertThat(result.isHasValue()).isTrue();
        assertThat(result.getBuckets()).isEmpty();
    }

    @Test
    @DisplayName("创建多值聚合 - ofBuckets方法 - 返回多值结果")
    void ofBuckets_ValidBuckets_ReturnsMultiValueResult() {
        // Arrange
        List<AggregationBucket> buckets = List.of(
            AggregationBucket.builder().key("electronics").docCount(100).build(),
            AggregationBucket.builder().key("clothing").docCount(200).build()
        );

        // Act
        SearchAggregationResult result = SearchAggregationResult.ofBuckets("by_category", buckets);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("by_category");
        assertThat(result.getBuckets()).hasSize(2);
        assertThat(result.isHasValue()).isFalse();
    }

    @Test
    @DisplayName("构建聚合结果 - 使用Builder - 成功构建")
    void build_UsingBuilder_Success() {
        // Arrange & Act
        SearchAggregationResult result = SearchAggregationResult.builder()
            .name("avg_price")
            .value(799.99)
            .hasValue(true)
            .build();

        // Assert
        assertThat(result.getName()).isEqualTo("avg_price");
        assertThat(result.getValue()).isEqualTo(799.99);
        assertThat(result.isHasValue()).isTrue();
    }

    @Test
    @DisplayName("聚合结果带子聚合 - buckets包含subAggregations - 成功构建")
    void build_WithSubAggregations_Success() {
        // Arrange
        List<AggregationBucket> buckets = List.of(
            AggregationBucket.builder()
                .key("electronics")
                .docCount(100)
                .subAggregations(List.of(
                    SearchAggregationResult.ofSingleValue("avg_price", 799.99)
                ))
                .build()
        );

        // Act
        SearchAggregationResult result = SearchAggregationResult.ofBuckets("by_category", buckets);

        // Assert
        assertThat(result.getBuckets()).hasSize(1);
        assertThat(result.getBuckets().get(0).getSubAggregations()).hasSize(1);
        assertThat(result.getBuckets().get(0).getSubAggregations().get(0).getName()).isEqualTo("avg_price");
    }

    @Test
    @DisplayName("使用默认值 - 不设置参数 - 使用默认值")
    void build_NoParams_UsesDefaults() {
        // Arrange & Act
        SearchAggregationResult result = SearchAggregationResult.builder().build();

        // Assert
        assertThat(result.getName()).isNull();
        assertThat(result.isHasValue()).isFalse();
        assertThat(result.getBuckets()).isNotNull();
        assertThat(result.getBuckets()).isEmpty();
    }

    @Test
    @DisplayName("使用NoArgsConstructor - 创建空对象 - 成功创建")
    void noArgsConstructor_CreateEmpty_Success() {
        // Arrange & Act
        SearchAggregationResult result = new SearchAggregationResult();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isNull();
        assertThat(result.isHasValue()).isFalse();
        assertThat(result.getBuckets()).isNotNull(); // 默认空List
    }
}
