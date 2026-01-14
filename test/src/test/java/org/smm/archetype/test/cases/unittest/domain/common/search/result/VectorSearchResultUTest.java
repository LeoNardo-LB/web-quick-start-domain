package org.smm.archetype.test.cases.unittest.domain.common.search.result;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.smm.archetype.domain.common.search.result.VectorSearchHit;
import org.smm.archetype.domain.common.search.result.VectorSearchResult;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * VectorSearchResult单元测试
 *
 * @author Leonardo
 * @since 2026-01-14
 */
@DisplayName("VectorSearchResult单元测试")
class VectorSearchResultUTest {

    @Test
    @DisplayName("构建向量搜索结果 - 完整参数 - 返回完整对象")
    void build_WithAllParams_ReturnsFullObject() {
        // Arrange
        List<Float> vector = List.of(0.1f, 0.2f, 0.3f);
        VectorSearchHit<List<Float>> hit1 = VectorSearchHit.<List<Float>>builder()
                .id("doc1")
                .score(0.95f)
                .document(vector)
                .distance(0.1)
                .extraInfo(Map.of("shard", 0))
                .build();

        VectorSearchHit<List<Float>> hit2 = VectorSearchHit.<List<Float>>builder()
                .id("doc2")
                .score(0.85f)
                .document(vector)
                .build();

        // Act
        VectorSearchResult<List<Float>> result = VectorSearchResult.<List<Float>>builder()
                .hits(List.of(hit1, hit2))
                .took(50L)
                .build();

        // Assert
        assertThat(result.getHits()).hasSize(2);
        assertThat(result.getTook()).isEqualTo(50L);
        assertThat(result.getHits().get(0).getId()).isEqualTo("doc1");
        assertThat(result.getHits().get(0).getScore()).isEqualTo(0.95f);
        assertThat(result.getHits().get(1).getId()).isEqualTo("doc2");
        assertThat(result.getHits().get(1).getScore()).isEqualTo(0.85f);
    }

    @Test
    @DisplayName("构建向量搜索结果 - 最小参数 - 返回必要字段")
    void build_WithMinimalParams_ReturnsRequiredFields() {
        // Arrange
        List<Float> vector = List.of(0.1f, 0.2f, 0.3f);
        VectorSearchHit<List<Float>> hit = VectorSearchHit.<List<Float>>builder()
                .id("doc1")
                .score(0.95f)
                .document(vector)
                .build();

        // Act
        VectorSearchResult<List<Float>> result = VectorSearchResult.<List<Float>>builder()
                .hits(List.of(hit))
                .build();

        // Assert
        assertThat(result.getHits()).hasSize(1);
        assertThat(result.getTook()).isNull();
        assertThat(result.getHits().get(0).getId()).isEqualTo("doc1");
    }

    @Test
    @DisplayName("构建向量搜索命中文档 - 包含距离值 - 验证距离")
    void buildHit_WithDistance_VerifiesDistance() {
        // Arrange
        List<Float> vector = List.of(0.1f, 0.2f, 0.3f);

        // Act
        VectorSearchHit<List<Float>> hit = VectorSearchHit.<List<Float>>builder()
                .id("doc1")
                .score(0.95f)
                .document(vector)
                .distance(0.123)
                .build();

        // Assert
        assertThat(hit.getDistance()).isEqualTo(0.123);
    }

    @Test
    @DisplayName("构建向量搜索命中文档 - 包含额外信息 - 验证额外信息")
    void buildHit_WithExtraInfo_VerifiesExtraInfo() {
        // Arrange
        List<Float> vector = List.of(0.1f, 0.2f, 0.3f);

        // Act
        VectorSearchHit<List<Float>> hit = VectorSearchHit.<List<Float>>builder()
                .id("doc1")
                .score(0.95f)
                .document(vector)
                .extraInfo(Map.of("shard", 0, "node", "node1"))
                .build();

        // Assert
        assertThat(hit.getExtraInfo()).hasSize(2);
        assertThat(hit.getExtraInfo().get("shard")).isEqualTo(0);
        assertThat(hit.getExtraInfo().get("node")).isEqualTo("node1");
    }

    @Test
    @DisplayName("构建向量搜索结果 - 空命中列表 - 返回空结果")
    void build_WithEmptyHits_ReturnsEmptyResult() {
        // Act
        VectorSearchResult<List<Float>> result = VectorSearchResult.<List<Float>>builder()
                .hits(List.of())
                .took(10L)
                .build();

        // Assert
        assertThat(result.getHits()).isEmpty();
        assertThat(result.getTook()).isEqualTo(10L);
    }
}
