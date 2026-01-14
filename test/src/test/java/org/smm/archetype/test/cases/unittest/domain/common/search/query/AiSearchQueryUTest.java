package org.smm.archetype.test.cases.unittest.domain.common.search.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.smm.archetype.domain.common.search.enums.AiSearchModelType;
import org.smm.archetype.domain.common.search.enums.RerankStrategyType;
import org.smm.archetype.domain.common.search.query.AiSearchQuery;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AiSearchQuery单元测试")
class AiSearchQueryUTest {

    @Test
    @DisplayName("构建AI搜索查询 - 最小参数 - 使用默认值")
    void build_WithMinimalParams_UsesDefaultValues() {
        AiSearchQuery query = AiSearchQuery.builder()
                .queryText("iPhone 15 review")
                .build();

        assertThat(query.getQueryText()).isEqualTo("iPhone 15 review");
        assertThat(query.getModelType()).isEqualTo(AiSearchModelType.ELSER);
        assertThat(query.getSize()).isEqualTo(10);
        assertThat(query.getFrom()).isEqualTo(0);
        assertThat(query.getRerankStrategy()).isEqualTo(RerankStrategyType.NONE);
    }

    @Test
    @DisplayName("构建AI搜索查询 - 启用重排序 - 验证参数")
    void build_WithRerankStrategy_VerifiesParams() {
        AiSearchQuery query = AiSearchQuery.builder()
                .queryText("iPhone 15")
                .rerankStrategy(RerankStrategyType.SCORE_WEIGHTED)
                .bm25Weight(0.7f)
                .build();

        assertThat(query.getRerankStrategy()).isEqualTo(RerankStrategyType.SCORE_WEIGHTED);
        assertThat(query.getBm25Weight()).isEqualTo(0.7f);
    }
}
