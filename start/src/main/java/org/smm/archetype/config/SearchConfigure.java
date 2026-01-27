package org.smm.archetype.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.config.properties.SearchProperties;
import org.smm.archetype.domain.bizshared.client.SearchClient;
import org.smm.archetype.domain.common.search.SearchService;
import org.smm.archetype.infrastructure.bizshared.client.search.impl.DisabledSearchClientImpl;
import org.smm.archetype.infrastructure.bizshared.client.search.impl.ElasticsearchClientImpl;
import org.smm.archetype.infrastructure.common.search.SearchServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

/**
 * 搜索服务配置类（Search Service Configuration）
 *
 * <p>配置ES客户端的Bean创建，根据ElasticsearchOperations Bean的存在性自动选择实现：
 * <ul>
 *   <li>ElasticsearchOperations Bean存在 → 创建 {@link ElasticsearchClientImpl}，使用Spring Data ES</li>
 *   <li>ElasticsearchOperations Bean不存在 → 创建 {@link DisabledSearchClientImpl}，禁用ES功能</li>
 * </ul>
 *
 * <p>条件装配规则：
 * <ul>
 *   <li>{@code esClient()}: {@code @ConditionalOnBean(ElasticsearchOperations.class)}</li>
 *   <li>{@code disabledEsClient()}: {@code @ConditionalOnMissingBean(ElasticsearchOperations.class)}</li>
 * </ul>
 *
 * <p>配置方式：
 * <ul>
 *   <li>启用ES：配置 {@code spring.elasticsearch.*} (Spring Boot标准配置)</li>
 *   <li>禁用ES：不配置ES依赖或移除 {@code spring.elasticsearch.*} 配置</li>
 * </ul>
 * @author Leonardo
 * @since 2026-01-24
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(SearchProperties.class)
@RequiredArgsConstructor
public class SearchConfigure {

    private final SearchProperties searchProperties;

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    @ConditionalOnBean(ElasticsearchOperations.class)
    public SearchClient esClient(ElasticsearchOperations operations) {
        log.info("Elasticsearch client initialized with ElasticsearchOperations");
        return new ElasticsearchClientImpl(operations);
    }

    @Bean
    @ConditionalOnMissingBean(ElasticsearchOperations.class)
    public SearchClient disabledEsClient() {
        log.info("Elasticsearch is disabled (ElasticsearchOperations bean not found)");
        return new DisabledSearchClientImpl();
    }

    @Bean
    public SearchService searchService(SearchClient searchClient, ObjectMapper objectMapper) {
        return new SearchServiceImpl(searchClient, objectMapper);
    }

}
