package cases.unittest.org.smm.archetype.domain.common.search.enum_;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.smm.archetype.domain.common.search.enums.SortOrder;
import support.UnitTestBase;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SortOrder枚举单元测试
 *
 * @author Leonardo
 * @since 2026-01-14
 */
@DisplayName("SortOrder枚举单元测试")
class SortOrderUTest extends UnitTestBase {

    @Test
    @DisplayName("ASC排序 - 升序")
    void asc_AscendingOrder() {
        assertThat(SortOrder.ASC).isNotNull();
        assertThat(SortOrder.ASC.name()).isEqualTo("ASC");
    }

    @Test
    @DisplayName("DESC排序 - 降序")
    void desc_DescendingOrder() {
        assertThat(SortOrder.DESC).isNotNull();
        assertThat(SortOrder.DESC.name()).isEqualTo("DESC");
    }

    @Test
    @DisplayName("values方法 - 返回所有排序方向")
    void values_ReturnsAllOrders() {
        SortOrder[] orders = SortOrder.values();
        assertThat(orders).hasSize(2);
        assertThat(orders).containsExactly(SortOrder.ASC, SortOrder.DESC);
    }
}
