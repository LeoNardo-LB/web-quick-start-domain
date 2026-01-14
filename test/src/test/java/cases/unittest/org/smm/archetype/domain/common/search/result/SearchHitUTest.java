package cases.unittest.org.smm.archetype.domain.common.search.result;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.smm.archetype.domain.common.search.result.SearchHit;
import support.UnitTestBase;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SearchHit单元测试
 *
 * @author Leonardo
 * @since 2026-01-14
 */
@DisplayName("SearchHit单元测试")
class SearchHitUTest extends UnitTestBase {

    @Test
    @DisplayName("构建命中文档 - 完整参数 - 成功构建")
    void build_FullParams_Success() {
        // Arrange
        String id = "P001";
        float score = 1.5f;
        String document = "iPhone 15";

        // Act
        SearchHit<String> hit = SearchHit.<String>builder()
            .id(id)
            .score(score)
            .document(document)
            .build();

        // Assert
        assertThat(hit).isNotNull();
        assertThat(hit.getId()).isEqualTo(id);
        assertThat(hit.getScore()).isEqualTo(score);
        assertThat(hit.getDocument()).isEqualTo(document);
    }

    @Test
    @DisplayName("构建命中文档 - 带高亮 - 包含高亮信息")
    void build_WithHighlights_ContainsHighlights() {
        // Arrange
        Map<String, java.util.List<String>> highlights = Map.of(
            "title", List.of("<em>iPhone</em> 15"),
            "description", List.of("Latest <em>iPhone</em> model")
        );

        // Act
        SearchHit<String> hit = SearchHit.<String>builder()
            .id("P001")
            .score(1.0f)
            .document("iPhone 15")
            .highlights(highlights)
            .build();

        // Assert
        assertThat(hit.getHighlights()).isNotNull();
        assertThat(hit.getHighlights()).hasSize(2);
        assertThat(hit.getHighlights()).containsKey("title");
        assertThat(hit.getHighlights().get("title")).contains("<em>iPhone</em> 15");
    }

    @Test
    @DisplayName("使用默认值 - 不设置高亮 - 使用空Map")
    void build_NoHighlights_UsesEmptyMap() {
        // Arrange & Act
        SearchHit<String> hit = SearchHit.<String>builder()
            .id("P001")
            .score(1.0f)
            .document("iPhone 15")
            .build();

        // Assert
        assertThat(hit.getHighlights()).isNotNull();
        assertThat(hit.getHighlights()).isEmpty();
    }

    @Test
    @DisplayName("泛型支持 - 不同文档类型 - 支持泛型")
    void build_GenericType_SupportsGenericType() {
        // Arrange
        class Product {
            String name;
            double price;
        }

        Product product = new Product();
        product.name = "iPhone 15";
        product.price = 799.99;

        // Act
        SearchHit<Product> hit = SearchHit.<Product>builder()
            .id("P001")
            .score(1.0f)
            .document(product)
            .build();

        // Assert
        assertThat(hit.getDocument()).isNotNull();
        assertThat(hit.getDocument().name).isEqualTo("iPhone 15");
        assertThat(hit.getDocument().price).isEqualTo(799.99);
    }

    @Test
    @DisplayName("使用NoArgsConstructor - 创建空对象 - 成功创建")
    void noArgsConstructor_CreateEmpty_Success() {
        // Arrange & Act
        SearchHit<String> hit = new SearchHit<>();

        // Assert
        assertThat(hit).isNotNull();
        assertThat(hit.getId()).isNull();
        assertThat(hit.getScore()).isEqualTo(0.0f);
        assertThat(hit.getDocument()).isNull();
        assertThat(hit.getHighlights()).isNotNull(); // 默认空Map
    }
}
