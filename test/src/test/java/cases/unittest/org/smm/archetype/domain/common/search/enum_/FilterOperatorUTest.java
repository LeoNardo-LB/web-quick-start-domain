package cases.unittest.org.smm.archetype.domain.common.search.enum_;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.smm.archetype.domain.common.search.enums.FilterOperator;
import support.UnitTestBase;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * FilterOperator枚举单元测试
 *
 * @author Leonardo
 * @since 2026-01-14
 */
@DisplayName("FilterOperator枚举单元测试")
class FilterOperatorUTest extends UnitTestBase {

    @Test
    @DisplayName("EQ操作符 - 支持等于比较")
    void eq_Supported_ReturnsTrue() {
        assertThat(FilterOperator.EQ).isNotNull();
        assertThat(FilterOperator.EQ.name()).isEqualTo("EQ");
    }

    @Test
    @DisplayName("GT操作符 - 支持大于比较")
    void gt_Supported_ReturnsTrue() {
        assertThat(FilterOperator.GT).isNotNull();
        assertThat(FilterOperator.GT.name()).isEqualTo("GT");
    }

    @Test
    @DisplayName("RANGE操作符 - 支持范围查询")
    void range_Supported_ReturnsTrue() {
        assertThat(FilterOperator.RANGE).isNotNull();
        assertThat(FilterOperator.RANGE.name()).isEqualTo("RANGE");
    }

    @Test
    @DisplayName("IN操作符 - 支持列表查询")
    void in_Supported_ReturnsTrue() {
        assertThat(FilterOperator.IN).isNotNull();
        assertThat(FilterOperator.IN.name()).isEqualTo("IN");
    }

    @Test
    @DisplayName("values方法 - 返回所有操作符")
    void values_ReturnsAllOperators() {
        FilterOperator[] operators = FilterOperator.values();
        assertThat(operators).hasSize(14);
    }
}
