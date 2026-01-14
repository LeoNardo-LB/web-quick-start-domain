package org.smm.archetype.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.smm.archetype.domain._shared.client.EsClient;
import org.smm.archetype.domain.common.search.SearchService;
import org.smm.archetype.infrastructure._shared.client.es.ElasticsearchClientImpl;
import org.smm.archetype.infrastructure._shared.client.es.impl.MemoryEsClientImpl;
import org.smm.archetype.infrastructure.common.search.SearchServiceImpl;
import org.smm.archetype.config.properties.SearchProperties;
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
     * Elasticsearch REST 客户端
     *
     * <p>手动创建 RestClient，连接到配置的 ES 服务器
     */
    @Bean
    @ConditionalOnProperty(
        prefix = "middleware.search",
        name = "type",
        havingValue = "elasticsearch",
        matchIfMissing = true
    )
    public RestClient elasticsearchRestClient(SearchProperties properties) {
        String endpoint = properties.getElasticsearch().getEndpoint();
        // 解析 endpoint，格式为 http://localhost:9200
        String[] parts = endpoint.replace("http://", "").replace("https://", "").split(":");
        String host = parts[0];
        int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 9200;
        String scheme = endpoint.startsWith("https://") ? "https" : "http";

        return RestClient.builder(new HttpHost(host, port, scheme)).build();
    }

    /**
     * Elasticsearch 客户端
     *
     * <p>使用 RestClientTransport 创建 ElasticsearchClient
     */
    @Bean
    @ConditionalOnProperty(
        prefix = "middleware.search",
        name = "type",
        havingValue = "elasticsearch",
        matchIfMissing = true
    )
    public ElasticsearchClient elasticsearchClient(RestClient restClient) {
        // 创建 ElasticsearchClient
        RestClientTransport transport = new RestClientTransport(
            restClient,
            new JacksonJsonpMapper(objectMapper)
        );
        return new ElasticsearchClient(transport);
    }

    /**
     * ES客户端（优先）
     *
     * <p>当配置 middleware.search.type=elasticsearch 时启用
     */
    @Bean
    @ConditionalOnProperty(
        prefix = "middleware.search",
        name = "type",
        havingValue = "elasticsearch",
        matchIfMissing = true
    )
    public EsClient esClient(ElasticsearchClient elasticsearchClient) {
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
