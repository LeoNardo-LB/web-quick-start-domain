package org.smm.archetype.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.smm.archetype.domain._shared.client.EsClient;
import org.smm.archetype.domain.common.search.SearchService;
import org.smm.archetype.infrastructure._shared.client.es.ElasticsearchClientImpl;
import org.smm.archetype.infrastructure._shared.client.es.impl.MemoryEsClientImpl;
import org.smm.archetype.infrastructure.common.search.SearchServiceImpl;
import org.smm.archetype.config.properties.SearchProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 搜索配置类
 *
 * <p>负责搜索相关Bean的装配
 *
 * @author Leonardo
 * @since 2026-01-14
 */
@Configuration
@EnableConfigurationProperties(SearchProperties.class)
@RequiredArgsConstructor
public class SearchConfigure {

    private final ObjectMapper objectMapper;

    /**
     * Elasticsearch客户端（优先）
     *
     * <p>使用Spring Data Elasticsearch自动配置的ElasticsearchClient
     * <p>当配置 middleware.search.type=elasticsearch 且存在ElasticsearchClient Bean时启用
     */
    @Bean
    @ConditionalOnProperty(
        prefix = "middleware.search",
        name = "type",
        havingValue = "elasticsearch",
        matchIfMissing = true
    )
    @ConditionalOnBean(ElasticsearchClient.class)
    public EsClient elasticsearchClient(ElasticsearchClient elasticsearchClient) {
        return new ElasticsearchClientImpl(elasticsearchClient);
    }

    /**
     * 内存ES客户端（降级方案）
     *
     * <p>当配置 middleware.search.type=memory 时启用
     */
    @Bean
    @ConditionalOnProperty(
        prefix = "middleware.search",
        name = "type",
        havingValue = "memory"
    )
    public EsClient memoryEsClient(SearchProperties properties) {
        String dataPath = properties.getMemory().getDataPath();
        return new MemoryEsClientImpl(dataPath);
    }

    /**
     * 搜索服务
     */
    @Bean
    public SearchService searchService(EsClient esClient) {
        return new SearchServiceImpl(esClient, objectMapper);
    }
}
