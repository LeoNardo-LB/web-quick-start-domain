package org.smm.archetype.test.cases.unittest.domain.common.search.enum_;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.smm.archetype.domain.common.search.enums.VectorIndexType;
import support.UnitTestBase;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * VectorIndexType枚举单元测试
 *
 * @author Leonardo
 * @since 2026-01-14
 */
@DisplayName("VectorIndexType枚举单元测试")
class VectorIndexTypeUTest extends UnitTestBase {

    @Test
    @DisplayName("HNSW索引类型 - 枚举值存在")
    void hnw_EnumValueExists_ReturnsTrue() {
        // Act & Assert
        assertThat(VectorIndexType.HNSW).isNotNull();
        assertThat(VectorIndexType.HNSW.name()).isEqualTo("HNSW");
    }

    @Test
    @DisplayName("IVF索引类型 - 枚举值存在")
    void ivf_EnumValueExists_ReturnsTrue() {
        // Act & Assert
        assertThat(VectorIndexType.IVF).isNotNull();
        assertThat(VectorIndexType.IVF.name()).isEqualTo("IVF");
    }

    @Test
    @DisplayName("FLAT索引类型 - 枚举值存在")
    void flat_EnumValueExists_ReturnsTrue() {
        // Act & Assert
        assertThat(VectorIndexType.FLAT).isNotNull();
        assertThat(VectorIndexType.FLAT.name()).isEqualTo("FLAT");
    }

    @Test
    @DisplayName("枚举值数量 - 验证数量正确")
    void values_Count_ReturnsThree() {
        // Act
        VectorIndexType[] values = VectorIndexType.values();

        // Assert
        assertThat(values).hasSize(3);
    }

    @Test
    @DisplayName("valueOf方法 - 正确字符串 - 返回对应枚举")
    void valueOf_ValidString_ReturnsEnum() {
        // Act & Assert
        assertThat(VectorIndexType.valueOf("HNSW")).isEqualTo(VectorIndexType.HNSW);
        assertThat(VectorIndexType.valueOf("IVF")).isEqualTo(VectorIndexType.IVF);
        assertThat(VectorIndexType.valueOf("FLAT")).isEqualTo(VectorIndexType.FLAT);
    }
}
