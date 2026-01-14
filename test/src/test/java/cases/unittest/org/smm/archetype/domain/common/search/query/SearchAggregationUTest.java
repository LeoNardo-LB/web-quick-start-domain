package cases.unittest.org.smm.archetype.domain.common.search.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.smm.archetype.domain.common.search.enums.AggregationType;
import org.smm.archetype.domain.common.search.query.SearchAggregation;
import support.UnitTestBase;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SearchAggregation单元测试
 *
 * @author Leonardo
 * @since 2026-01-14
 */
@DisplayName("SearchAggregation单元测试")
class SearchAggregationUTest extends UnitTestBase {

    @Test
    @DisplayName("构建聚合 - TERMS聚合 - 成功构建")
    void build_TermsAggregation_Success() {
        // Arrange & Act
        SearchAggregation agg = SearchAggregation.builder()
            .name("by_category")
            .type(AggregationType.TERMS)
            .field("category")
            .size(10)
            .build();

        // Assert
        assertThat(agg).isNotNull();
        assertThat(agg.getName()).isEqualTo("by_category");
        assertThat(agg.getType()).isEqualTo(AggregationType.TERMS);
        assertThat(agg.getField()).isEqualTo("category");
        assertThat(agg.getSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("构建统计聚合 - AVG聚合 - 成功构建")
    void build_AvgAggregation_Success() {
        // Arrange & Act
        SearchAggregation agg = SearchAggregation.builder()
            .name("avg_price")
            .type(AggregationType.AVG)
            .field("price")
            .build();

        // Assert
        assertThat(agg.getName()).isEqualTo("avg_price");
        assertThat(agg.getType()).isEqualTo(AggregationType.AVG);
        assertThat(agg.getField()).isEqualTo("price");
    }

    @Test
    @DisplayName("使用默认size - 不设置size - 使用默认值10")
    void build_NoSize_UsesDefault() {
        // Arrange & Act
        SearchAggregation agg = SearchAggregation.builder()
            .name("test")
            .type(AggregationType.TERMS)
            .field("category")
            .build();

        // Assert
        assertThat(agg.getSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("添加子聚合 - 包含子聚合 - 成功添加")
    void build_WithSubAggregation_Success() {
        // Arrange
        SearchAggregation subAgg = SearchAggregation.builder()
            .name("avg_price")
            .type(AggregationType.AVG)
            .field("price")
            .build();

        // Act
        SearchAggregation agg = SearchAggregation.builder()
            .name("by_category")
            .type(AggregationType.TERMS)
            .field("category")
            .subAggregations(List.of(subAgg))
            .build();

        // Assert
        assertThat(agg.getSubAggregations()).hasSize(1);
        assertThat(agg.getSubAggregations().get(0).getName()).isEqualTo("avg_price");
    }

    @Test
    @DisplayName("使用script聚合 - 设置脚本 - 成功构建")
    void build_ScriptAggregation_Success() {
        // Arrange & Act
        SearchAggregation agg = SearchAggregation.builder()
            .name("custom_script")
            .type(AggregationType.TERMS)
            .script("doc.price.value * params.multiplier")
            .build();

        // Assert
        assertThat(agg.getScript()).isNotNull();
        assertThat(agg.getScript()).contains("doc.price.value");
    }

    @Test
    @DisplayName("使用NoArgsConstructor - 创建空对象 - 成功创建")
    void noArgsConstructor_CreateEmpty_Success() {
        // Arrange & Act
        SearchAggregation agg = new SearchAggregation();

        // Assert
        assertThat(agg).isNotNull();
        assertThat(agg.getName()).isNull();
        assertThat(agg.getType()).isNull();
        assertThat(agg.getSize()).isEqualTo(10); // 默认值
    }
}
