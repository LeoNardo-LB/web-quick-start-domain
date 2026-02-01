package org.smm.archetype.test.cases.unittest.domain.common.search.enum_;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.smm.archetype.domain.common.search.enums.AiSearchModelType;
import support.UnitTestBase;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AiSearchModelType枚举单元测试
 *


 */
@DisplayName("AiSearchModelType枚举单元测试")
class AiSearchModelTypeUTest extends UnitTestBase {

    @Test
    @DisplayName("ELSER模型 - 枚举值存在")
    void elser_EnumValueExists_ReturnsTrue() {
        assertThat(AiSearchModelType.ELSER).isNotNull();
        assertThat(AiSearchModelType.ELSER.name()).isEqualTo("ELSER");
    }

    @Test
    @DisplayName("COHERE_EMBED模型 - 枚举值存在")
    void cohereEmbed_EnumValueExists_ReturnsTrue() {
        assertThat(AiSearchModelType.COHERE_EMBED).isNotNull();
    }

    @Test
    @DisplayName("OPENAI_EMBEDDING模型 - 枚举值存在")
    void openaiEmbedding_EnumValueExists_ReturnsTrue() {
        assertThat(AiSearchModelType.OPENAI_EMBEDDING).isNotNull();
    }

    @Test
    @DisplayName("枚举值数量 - 验证数量正确")
    void values_Count_ReturnsThree() {
        assertThat(AiSearchModelType.values()).hasSize(3);
    }
}
