package org.smm.archetype.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain.shared.client.SearchClient;
import org.smm.archetype.domain.platform.search.SearchService;
import org.smm.archetype.infrastructure.platform.search.SearchServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    // @Bean
    // @Primary
    // @ConditionalOnBooleanProperty("spring.elasticsearch")
    // public SearchClient esClient(ElasticsearchOperations operations) {
    //     log.info("Elasticsearch client initialized with ElasticsearchOperations");
    //     return new ElasticsearchClientImpl(operations);
    // }

    @Bean
    @ConditionalOnBean
    public SearchService searchService(SearchClient searchClient, ObjectMapper objectMapper) {
        return new SearchServiceImpl(searchClient, objectMapper);
    }

}
