package cases.unittest.org.smm.archetype.domain.common.search.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.smm.archetype.domain.common.search.enums.FilterOperator;
import org.smm.archetype.domain.common.search.query.SearchFilter;
import support.UnitTestBase;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SearchFilter单元测试。
 */
@DisplayName("SearchFilter单元测试")
class SearchFilterUTest extends UnitTestBase {

    @Test
    @DisplayName("构建过滤条件 - EQ操作符 - 成功构建")
    void build_EqOperator_Success() {
        // Arrange & Act
        SearchFilter filter = SearchFilter.builder()
            .field("category")
            .operator(FilterOperator.EQ)
            .value("electronics")
            .build();

        // Assert
        assertThat(filter).isNotNull();
        assertThat(filter.getField()).isEqualTo("category");
        assertThat(filter.getOperator()).isEqualTo(FilterOperator.EQ);
        assertThat(filter.getValue()).isEqualTo("electronics");
    }

    @Test
    @DisplayName("构建范围过滤 - RANGE操作符 - 成功构建")
    void build_RangeOperator_Success() {
        // Arrange & Act
        SearchFilter.RangeValue rangeValue = SearchFilter.RangeValue.builder()
            .gte(100)
            .lte(1000)
            .build();

        SearchFilter filter = SearchFilter.builder()
            .field("price")
            .operator(FilterOperator.RANGE)
            .rangeValue(rangeValue)
            .build();

        // Assert
        assertThat(filter.getField()).isEqualTo("price");
        assertThat(filter.getOperator()).isEqualTo(FilterOperator.RANGE);
        assertThat(filter.getRangeValue()).isNotNull();
        assertThat(filter.getRangeValue().getGte()).isEqualTo(100);
        assertThat(filter.getRangeValue().getLte()).isEqualTo(1000);
    }

    @Test
    @DisplayName("构建IN过滤 - IN操作符 - 成功构建")
    void build_InOperator_Success() {
        // Arrange & Act
        SearchFilter filter = SearchFilter.builder()
            .field("status")
            .operator(FilterOperator.IN)
            .values(List.of("active", "pending"))
            .build();

        // Assert
        assertThat(filter.getField()).isEqualTo("status");
        assertThat(filter.getOperator()).isEqualTo(FilterOperator.IN);
        assertThat(filter.getValues()).hasSize(2);
        assertThat(filter.getValues()).contains("active", "pending");
    }

    @Test
    @DisplayName("RangeValue - 完整范围 - 所有字段设置")
    void rangeValue_FullRange_AllFieldsSet() {
        // Arrange & Act
        SearchFilter.RangeValue rangeValue = SearchFilter.RangeValue.builder()
            .gt(100)
            .gte(150)
            .lt(200)
            .lte(250)
            .build();

        // Assert
        assertThat(rangeValue.getGt()).isEqualTo(100);
        assertThat(rangeValue.getGte()).isEqualTo(150);
        assertThat(rangeValue.getLt()).isEqualTo(200);
        assertThat(rangeValue.getLte()).isEqualTo(250);
    }

    @Test
    @DisplayName("使用NoArgsConstructor - 创建空对象 - 成功创建")
    void noArgsConstructor_CreateEmpty_Success() {
        // Arrange & Act
        SearchFilter filter = new SearchFilter();

        // Assert
        assertThat(filter).isNotNull();
        assertThat(filter.getField()).isNull();
        assertThat(filter.getOperator()).isNull();
    }
}
