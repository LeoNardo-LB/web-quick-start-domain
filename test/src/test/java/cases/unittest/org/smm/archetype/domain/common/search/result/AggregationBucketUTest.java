package cases.unittest.org.smm.archetype.domain.common.search.result;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.smm.archetype.domain.common.search.result.AggregationBucket;
import org.smm.archetype.domain.common.search.result.SearchAggregationResult;
import support.UnitTestBase;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AggregationBucket单元测试。
 */
@DisplayName("AggregationBucket单元测试")
class AggregationBucketUTest extends UnitTestBase {

    @Test
    @DisplayName("构建聚合桶 - 完整参数 - 成功构建")
    void build_FullParams_Success() {
        // Arrange & Act
        AggregationBucket bucket = AggregationBucket.builder()
            .key("electronics")
            .docCount(100)
            .build();

        // Assert
        assertThat(bucket).isNotNull();
        assertThat(bucket.getKey()).isEqualTo("electronics");
        assertThat(bucket.getDocCount()).isEqualTo(100);
    }

    @Test
    @DisplayName("构建聚合桶 - 带子聚合 - 包含子聚合")
    void build_WithSubAggregations_ContainsSubAggs() {
        // Arrange
        SearchAggregationResult subAgg = SearchAggregationResult.ofSingleValue("avg_price", 799.99);

        // Act
        AggregationBucket bucket = AggregationBucket.builder()
            .key("electronics")
            .docCount(100)
            .subAggregations(List.of(subAgg))
            .build();

        // Assert
        assertThat(bucket.getSubAggregations()).hasSize(1);
        assertThat(bucket.getSubAggregations().get(0).getName()).isEqualTo("avg_price");
        assertThat(bucket.getSubAggregations().get(0).getValue()).isEqualTo(799.99);
    }

    @Test
    @DisplayName("使用默认值 - 不设置子聚合 - 使用空列表")
    void build_NoSubAggregations_UsesEmptyList() {
        // Arrange & Act
        AggregationBucket bucket = AggregationBucket.builder()
            .key("electronics")
            .docCount(100)
            .build();

        // Assert
        assertThat(bucket.getSubAggregations()).isNotNull();
        assertThat(bucket.getSubAggregations()).isEmpty();
    }

    @Test
    @DisplayName("使用NoArgsConstructor - 创建空对象 - 成功创建")
    void noArgsConstructor_CreateEmpty_Success() {
        // Arrange & Act
        AggregationBucket bucket = new AggregationBucket();

        // Assert
        assertThat(bucket).isNotNull();
        assertThat(bucket.getKey()).isNull();
        assertThat(bucket.getDocCount()).isEqualTo(0);
        assertThat(bucket.getSubAggregations()).isNotNull(); // 默认空List
    }

    @Test
    @DisplayName("构建多个桶 - 多个key - 每个桶独立")
    void build_MultipleBuckets_EachBucketIndependent() {
        // Arrange & Act
        AggregationBucket bucket1 = AggregationBucket.builder()
            .key("electronics")
            .docCount(100)
            .build();

        AggregationBucket bucket2 = AggregationBucket.builder()
            .key("clothing")
            .docCount(200)
            .build();

        // Assert
        assertThat(bucket1.getKey()).isNotEqualTo(bucket2.getKey());
        assertThat(bucket1.getDocCount()).isEqualTo(100);
        assertThat(bucket2.getDocCount()).isEqualTo(200);
    }
}
