package cases.unittest.org.smm.archetype.domain.common.search.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.smm.archetype.domain.common.search.enums.SearchStrategy;
import org.smm.archetype.domain.common.search.query.SearchQuery;
import support.UnitTestBase;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SearchQuery单元测试
 *
 * @author Leonardo
 * @since 2026-01-14
 */
@DisplayName("SearchQuery单元测试")
class SearchQueryUTest extends UnitTestBase {

    @Test
    @DisplayName("构建搜索查询 - 正常参数 - 返回完整对象")
    void build_ValidParams_ReturnsSearchQuery() {
        // Arrange & Act
        SearchQuery query = SearchQuery.builder()
            .keyword("iPhone")
            .strategy(SearchStrategy.BM25)
            .from(0)
            .size(10)
            .build();

        // Assert
        assertThat(query).isNotNull();
        assertThat(query.getKeyword()).isEqualTo("iPhone");
        assertThat(query.getStrategy()).isEqualTo(SearchStrategy.BM25);
        assertThat(query.getFrom()).isEqualTo(0);
        assertThat(query.getSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("使用默认值 - 不设置参数 - 使用默认值")
    void build_NoParams_UsesDefaults() {
        // Arrange & Act
        SearchQuery query = SearchQuery.builder().build();

        // Assert
        assertThat(query.getStrategy()).isEqualTo(SearchStrategy.BM25);
        assertThat(query.getFrom()).isEqualTo(0);
        assertThat(query.getSize()).isEqualTo(10);
        assertThat(query.isHighlight()).isFalse();
        assertThat(query.getMinScore()).isEqualTo(0.0f);
    }

    @Test
    @DisplayName("设置过滤条件 - 添加过滤器 - 成功添加")
    void build_WithFilters_Success() {
        // Arrange & Act
        SearchQuery query = SearchQuery.builder()
            .keyword("test")
            .filters(List.of())
            .build();

        // Assert
        assertThat(query.getFilters()).isNotNull();
        assertThat(query.getFilters()).isEmpty();
    }

    @Test
    @DisplayName("设置分页参数 - from和size - 正确设置")
    void build_Pagination_ParamsCorrect() {
        // Arrange & Act
        SearchQuery query = SearchQuery.builder()
            .from(20)
            .size(50)
            .build();

        // Assert
        assertThat(query.getFrom()).isEqualTo(20);
        assertThat(query.getSize()).isEqualTo(50);
    }

    @Test
    @DisplayName("设置高亮 - 启用高亮 - 高亮参数正确")
    void build_WithHighlight_HighlightEnabled() {
        // Arrange & Act
        SearchQuery query = SearchQuery.builder()
            .highlight(true)
            .highlightFields(List.of("title", "description"))
            .build();

        // Assert
        assertThat(query.isHighlight()).isTrue();
        assertThat(query.getHighlightFields()).hasSize(2);
        assertThat(query.getHighlightFields()).contains("title", "description");
    }

    @Test
    @DisplayName("设置最小得分 - minScore参数 - 正确设置")
    void build_WithMinScore_MinScoreSet() {
        // Arrange & Act
        SearchQuery query = SearchQuery.builder()
            .minScore(0.5f)
            .build();

        // Assert
        assertThat(query.getMinScore()).isEqualTo(0.5f);
    }
}
