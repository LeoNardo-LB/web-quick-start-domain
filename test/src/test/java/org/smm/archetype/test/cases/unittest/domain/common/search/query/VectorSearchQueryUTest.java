package org.smm.archetype.test.cases.unittest.domain.common.search.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.smm.archetype.domain.common.search.enums.VectorDistanceType;
import org.smm.archetype.domain.common.search.enums.VectorIndexType;
import org.smm.archetype.domain.common.search.query.VectorSearchQuery;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * VectorSearchQuery单元测试
 *
 * @author Leonardo
 * @since 2026-01-14
 */
@DisplayName("VectorSearchQuery单元测试")
class VectorSearchQueryUTest {

    @Test
    @DisplayName("构建向量搜索查询 - 最小参数 - 使用默认值")
    void build_WithMinimalParams_UsesDefaultValues() {
        // Arrange
        List<Float> vector = List.of(0.1f, 0.2f, 0.3f);

        // Act
        VectorSearchQuery query = VectorSearchQuery.builder()
                .vector(vector)
                .build();

        // Assert
        assertThat(query.getVector()).isEqualTo(vector);
        assertThat(query.getVectorField()).isEqualTo("vector");
        assertThat(query.getK()).isEqualTo(10);
        assertThat(query.getDistanceType()).isEqualTo(VectorDistanceType.COSINE);
        assertThat(query.getIndexType()).isNull();
        assertThat(query.getFilters()).isNull();
        assertThat(query.getNprobes()).isNull();
        assertThat(query.getEfSearch()).isNull();
    }

    @Test
    @DisplayName("构建向量搜索查询 - 完整参数 - 返回完整对象")
    void build_WithAllParams_ReturnsFullObject() {
        // Arrange
        List<Float> vector = List.of(0.1f, 0.2f, 0.3f);

        // Act
        VectorSearchQuery query = VectorSearchQuery.builder()
                .vector(vector)
                .vectorField("embedding")
                .k(20)
                .indexType(VectorIndexType.HNSW)
                .distanceType(VectorDistanceType.L2)
                .nprobes(10)
                .efSearch(100)
                .build();

        // Assert
        assertThat(query.getVector()).isEqualTo(vector);
        assertThat(query.getVectorField()).isEqualTo("embedding");
        assertThat(query.getK()).isEqualTo(20);
        assertThat(query.getIndexType()).isEqualTo(VectorIndexType.HNSW);
        assertThat(query.getDistanceType()).isEqualTo(VectorDistanceType.L2);
        assertThat(query.getNprobes()).isEqualTo(10);
        assertThat(query.getEfSearch()).isEqualTo(100);
    }

    @Test
    @DisplayName("构建向量搜索查询 - 不同距离类型 - 验证枚举值")
    void build_WithDifferentDistanceTypes_VerifiesEnumValues() {
        // Arrange
        List<Float> vector = List.of(0.1f, 0.2f, 0.3f);

        // Act & Assert
        VectorSearchQuery cosineQuery = VectorSearchQuery.builder()
                .vector(vector)
                .distanceType(VectorDistanceType.COSINE)
                .build();
        assertThat(cosineQuery.getDistanceType()).isEqualTo(VectorDistanceType.COSINE);

        VectorSearchQuery l2Query = VectorSearchQuery.builder()
                .vector(vector)
                .distanceType(VectorDistanceType.L2)
                .build();
        assertThat(l2Query.getDistanceType()).isEqualTo(VectorDistanceType.L2);

        VectorSearchQuery dotProductQuery = VectorSearchQuery.builder()
                .vector(vector)
                .distanceType(VectorDistanceType.DOT_PRODUCT)
                .build();
        assertThat(dotProductQuery.getDistanceType()).isEqualTo(VectorDistanceType.DOT_PRODUCT);
    }

    @Test
    @DisplayName("构建向量搜索查询 - 不同索引类型 - 验证枚举值")
    void build_WithDifferentIndexTypes_VerifiesEnumValues() {
        // Arrange
        List<Float> vector = List.of(0.1f, 0.2f, 0.3f);

        // Act & Assert
        VectorSearchQuery hnswQuery = VectorSearchQuery.builder()
                .vector(vector)
                .indexType(VectorIndexType.HNSW)
                .build();
        assertThat(hnswQuery.getIndexType()).isEqualTo(VectorIndexType.HNSW);

        VectorSearchQuery ivfQuery = VectorSearchQuery.builder()
                .vector(vector)
                .indexType(VectorIndexType.IVF)
                .build();
        assertThat(ivfQuery.getIndexType()).isEqualTo(VectorIndexType.IVF);

        VectorSearchQuery flatQuery = VectorSearchQuery.builder()
                .vector(vector)
                .indexType(VectorIndexType.FLAT)
                .build();
        assertThat(flatQuery.getIndexType()).isEqualTo(VectorIndexType.FLAT);
    }
}
