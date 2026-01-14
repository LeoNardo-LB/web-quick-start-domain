package cases.unittest.org.smm.archetype.domain.common.search.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.smm.archetype.domain.common.search.enums.SortOrder;
import org.smm.archetype.domain.common.search.query.SearchSort;
import support.UnitTestBase;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SearchSort单元测试
 *
 * @author Leonardo
 * @since 2026-01-14
 */
@DisplayName("SearchSort单元测试")
class SearchSortUTest extends UnitTestBase {

    @Test
    @DisplayName("构建排序条件 - ASC排序 - 成功构建")
    void build_AscOrder_Success() {
        // Arrange & Act
        SearchSort sort = SearchSort.builder()
            .field("price")
            .order(SortOrder.ASC)
            .build();

        // Assert
        assertThat(sort).isNotNull();
        assertThat(sort.getField()).isEqualTo("price");
        assertThat(sort.getOrder()).isEqualTo(SortOrder.ASC);
        assertThat(sort.isByScore()).isFalse();
    }

    @Test
    @DisplayName("构建排序条件 - DESC排序 - 成功构建")
    void build_DescOrder_Success() {
        // Arrange & Act
        SearchSort sort = SearchSort.builder()
            .field("createTime")
            .order(SortOrder.DESC)
            .build();

        // Assert
        assertThat(sort.getField()).isEqualTo("createTime");
        assertThat(sort.getOrder()).isEqualTo(SortOrder.DESC);
    }

    @Test
    @DisplayName("使用默认排序 - 不设置排序 - 使用ASC默认值")
    void build_NoOrder_UsesDefault() {
        // Arrange & Act
        SearchSort sort = SearchSort.builder()
            .field("name")
            .build();

        // Assert
        assertThat(sort.getOrder()).isEqualTo(SortOrder.ASC);
    }

    @Test
    @DisplayName("按得分排序 - byScore为true - 启用得分排序")
    void build_ByScore_True_Success() {
        // Arrange & Act
        SearchSort sort = SearchSort.builder()
            .byScore(true)
            .build();

        // Assert
        assertThat(sort.isByScore()).isTrue();
        assertThat(sort.getField()).isNull(); // byScore时不需要field
    }

    @Test
    @DisplayName("使用NoArgsConstructor - 创建空对象 - 成功创建")
    void noArgsConstructor_CreateEmpty_Success() {
        // Arrange & Act
        SearchSort sort = new SearchSort();

        // Assert
        assertThat(sort).isNotNull();
        assertThat(sort.getField()).isNull();
        assertThat(sort.getOrder()).isEqualTo(SortOrder.ASC); // 默认值
        assertThat(sort.isByScore()).isFalse(); // 默认值
    }
}
