package cases.unittest.org.smm.archetype.domain.common.search.enum_;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.smm.archetype.domain.common.search.enums.AggregationType;
import support.UnitTestBase;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AggregationType枚举单元测试
 *
 * @author Leonardo
 * @since 2026-01-14
 */
@DisplayName("AggregationType枚举单元测试")
class AggregationTypeUTest extends UnitTestBase {

    @Test
    @DisplayName("TERMS聚合 - 词项聚合")
    void terms_TermsAggregation() {
        assertThat(AggregationType.TERMS).isNotNull();
        assertThat(AggregationType.TERMS.name()).isEqualTo("TERMS");
    }

    @Test
    @DisplayName("SUM聚合 - 求和")
    void sum_SumAggregation() {
        assertThat(AggregationType.SUM).isNotNull();
        assertThat(AggregationType.SUM.name()).isEqualTo("SUM");
    }

    @Test
    @DisplayName("AVG聚合 - 平均值")
    void avg_AverageAggregation() {
        assertThat(AggregationType.AVG).isNotNull();
        assertThat(AggregationType.AVG.name()).isEqualTo("AVG");
    }

    @Test
    @DisplayName("values方法 - 返回所有聚合类型")
    void values_ReturnsAllTypes() {
        AggregationType[] types = AggregationType.values();
        assertThat(types).hasSize(10);
    }
}
