package cases.unittest.org.smm.archetype.domain.common.search.enum_;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.smm.archetype.domain.common.search.enums.SearchStrategy;
import support.UnitTestBase;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SearchStrategy枚举单元测试。
 */
@DisplayName("SearchStrategy枚举单元测试")
class SearchStrategyUTest extends UnitTestBase {

    @Test
    @DisplayName("BM25策略 - 存在且非空")
    void bm25_ExistsAndNotNull() {
        assertThat(SearchStrategy.BM25).isNotNull();
        assertThat(SearchStrategy.BM25.name()).isEqualTo("BM25");
    }

    @Test
    @DisplayName("VECTOR策略 - 存在且非空")
    void vector_ExistsAndNotNull() {
        assertThat(SearchStrategy.VECTOR).isNotNull();
        assertThat(SearchStrategy.VECTOR.name()).isEqualTo("VECTOR");
    }

    @Test
    @DisplayName("HYBRID策略 - 存在且非空")
    void hybrid_ExistsAndNotNull() {
        assertThat(SearchStrategy.HYBRID).isNotNull();
        assertThat(SearchStrategy.HYBRID.name()).isEqualTo("HYBRID");
    }

    @Test
    @DisplayName("SEMANTIC策略 - 存在且非空")
    void semantic_ExistsAndNotNull() {
        assertThat(SearchStrategy.SEMANTIC).isNotNull();
        assertThat(SearchStrategy.SEMANTIC.name()).isEqualTo("SEMANTIC");
    }

    @Test
    @DisplayName("values方法 - 返回所有策略")
    void values_ReturnsAllStrategies() {
        SearchStrategy[] strategies = SearchStrategy.values();
        assertThat(strategies).hasSize(4);
        assertThat(strategies).containsExactly(
            SearchStrategy.BM25,
            SearchStrategy.VECTOR,
            SearchStrategy.HYBRID,
            SearchStrategy.SEMANTIC
        );
    }
}
