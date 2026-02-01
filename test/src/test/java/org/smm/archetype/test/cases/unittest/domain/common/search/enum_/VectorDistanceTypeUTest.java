package org.smm.archetype.test.cases.unittest.domain.common.search.enum_;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.smm.archetype.domain.common.search.enums.VectorDistanceType;
import support.UnitTestBase;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * VectorDistanceType枚举单元测试。
 */
@DisplayName("VectorDistanceType枚举单元测试")
class VectorDistanceTypeUTest extends UnitTestBase {

    @Test
    @DisplayName("COSINE距离类型 - 枚举值存在")
    void cosine_EnumValueExists_ReturnsTrue() {
        // Act & Assert
        assertThat(VectorDistanceType.COSINE).isNotNull();
        assertThat(VectorDistanceType.COSINE.name()).isEqualTo("COSINE");
    }

    @Test
    @DisplayName("L2距离类型 - 枚举值存在")
    void l2_EnumValueExists_ReturnsTrue() {
        // Act & Assert
        assertThat(VectorDistanceType.L2).isNotNull();
        assertThat(VectorDistanceType.L2.name()).isEqualTo("L2");
    }

    @Test
    @DisplayName("DOT_PRODUCT距离类型 - 枚举值存在")
    void dotProduct_EnumValueExists_ReturnsTrue() {
        // Act & Assert
        assertThat(VectorDistanceType.DOT_PRODUCT).isNotNull();
        assertThat(VectorDistanceType.DOT_PRODUCT.name()).isEqualTo("DOT_PRODUCT");
    }

    @Test
    @DisplayName("枚举值数量 - 验证数量正确")
    void values_Count_ReturnsThree() {
        // Act
        VectorDistanceType[] values = VectorDistanceType.values();

        // Assert
        assertThat(values).hasSize(3);
    }

    @Test
    @DisplayName("valueOf方法 - 正确字符串 - 返回对应枚举")
    void valueOf_ValidString_ReturnsEnum() {
        // Act & Assert
        assertThat(VectorDistanceType.valueOf("COSINE")).isEqualTo(VectorDistanceType.COSINE);
        assertThat(VectorDistanceType.valueOf("L2")).isEqualTo(VectorDistanceType.L2);
        assertThat(VectorDistanceType.valueOf("DOT_PRODUCT")).isEqualTo(VectorDistanceType.DOT_PRODUCT);
    }
}
