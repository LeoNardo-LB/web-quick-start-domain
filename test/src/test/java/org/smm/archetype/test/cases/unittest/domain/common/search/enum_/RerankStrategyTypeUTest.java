package org.smm.archetype.test.cases.unittest.domain.common.search.enum_;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.smm.archetype.domain.common.search.enums.RerankStrategyType;
import support.UnitTestBase;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RerankStrategyType枚举单元测试")
class RerankStrategyTypeUTest extends UnitTestBase {

    @Test
    @DisplayName("所有枚举值 - 验证存在")
    void allEnums_Exist_ReturnsTrue() {
        assertThat(RerankStrategyType.NONE).isNotNull();
        assertThat(RerankStrategyType.SCORE_WEIGHTED).isNotNull();
        assertThat(RerankStrategyType.RRF).isNotNull();
        assertThat(RerankStrategyType.AI_MODEL).isNotNull();
    }

    @Test
    @DisplayName("枚举值数量 - 验证数量正确")
    void values_Count_ReturnsFour() {
        assertThat(RerankStrategyType.values()).hasSize(4);
    }
}
