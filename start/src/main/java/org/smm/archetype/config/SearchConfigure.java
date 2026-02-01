package org.smm.archetype.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain.bizshared.client.SearchClient;
import org.smm.archetype.domain.common.search.SearchService;
import org.smm.archetype.infrastructure.bizshared.client.search.impl.DisabledSearchClientImpl;
import org.smm.archetype.infrastructure.bizshared.client.search.impl.ElasticsearchClientImpl;
import org.smm.archetype.infrastructure.common.search.SearchServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

/**
 * 搜索服务配置类，自动检测Elasticsearch并配置搜索客户端。
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SearchConfigure {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    @Primary
    @ConditionalOnBooleanProperty("spring.elasticsearch")
    public SearchClient esClient(ElasticsearchOperations operations) {
        log.info("Elasticsearch client initialized with ElasticsearchOperations");
        return new ElasticsearchClientImpl(operations);
    }

    @Bean
    public SearchClient disabledEsClient() {
        log.info("Elasticsearch is disabled (ElasticsearchOperations bean not found)");
        return new DisabledSearchClientImpl();
    }

    @Bean
    public SearchService searchService(SearchClient searchClient, ObjectMapper objectMapper) {
        return new SearchServiceImpl(searchClient, objectMapper);
    }

}
