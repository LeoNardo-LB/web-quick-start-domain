package org.smm.archetype.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.cluster.HealthResponse;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.smm.archetype.config.properties.SearchProperties;
import org.smm.archetype.domain._shared.client.SearchClient;
import org.smm.archetype.domain.common.search.SearchService;
import org.smm.archetype.infrastructure._shared.client.search.impl.DisabledSearchClientImpl;
import org.smm.archetype.infrastructure._shared.client.search.impl.ElasticsearchClientImpl;
import org.smm.archetype.infrastructure.common.search.SearchServiceImpl;
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
@Slf4j
@Configuration
@EnableConfigurationProperties(SearchProperties.class)
@RequiredArgsConstructor
public class SearchConfigure {

    private final ObjectMapper     objectMapper;
    private final SearchProperties searchProperties;

    /**
     * Elasticsearch REST 客户端
     *
     * <p>条件：middleware.search.enabled=true
     */
    @Bean
    @ConditionalOnProperty(
        prefix = "middleware.search",
        name = "enabled",
        havingValue = "true"
    )
    public RestClient elasticsearchRestClient(SearchProperties properties) {
        String endpoint = properties.getElasticsearch().getEndpoint();
        // 解析 endpoint，格式为 http://localhost:9200
        String[] parts = endpoint.replace("http://", "").replace("https://", "").split(":");
        String host = parts[0];
        int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 9200;
        String scheme = endpoint.startsWith("https://") ? "https" : "http";

        log.info("Initializing Elasticsearch REST client: {}:{}", host, port);
        return RestClient.builder(new HttpHost(host, port, scheme)).build();
    }

    /**
     * Elasticsearch 客户端
     *
     * <p>条件：middleware.search.enabled=true
     */
    @Bean
    @ConditionalOnProperty(
        prefix = "middleware.search",
        name = "enabled",
        havingValue = "true"
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
     * ES客户端（真实实现）
     *
     * <p>条件：middleware.search.enabled=true
     *
     * <p>启动时会检查 ES 连接可用性，如果连接失败则快速失败（抛出异常）
     */
    @Bean
    @ConditionalOnProperty(
        prefix = "middleware.search",
        name = "enabled",
        havingValue = "true"
    )
    public SearchClient esClient(ElasticsearchClient elasticsearchClient, SearchProperties properties) {
        // ES 健康检查：确保 ES 可用
        try {
            HealthResponse health = elasticsearchClient.cluster().health();
            String clusterName = health.clusterName();
            String status = health.status().jsonValue();

            log.info("Elasticsearch is available: cluster={}, status={}", clusterName, status);

            // 如果集群状态为 red，警告但不阻止启动
            if ("red".equalsIgnoreCase(status)) {
                log.warn("Elasticsearch cluster status is RED, please check cluster health");
            }
        } catch (Exception e) {
            throw new IllegalStateException(
                "Elasticsearch is enabled but not available at: " +
                properties.getElasticsearch().getEndpoint() +
                ". Please check ES is running or set middleware.search.enabled=false",
                e
            );
        }

        return new ElasticsearchClientImpl(elasticsearchClient);
    }

    /**
     * ES客户端（禁用实现）
     *
     * <p>条件：middleware.search.enabled=false（默认）
     *
     * <p>所有操作抛出 IllegalStateException，明确提示 ES 未启用
     */
    @Bean
    @ConditionalOnProperty(
        prefix = "middleware.search",
        name = "enabled",
        havingValue = "false",
        matchIfMissing = true  // 默认启用
    )
    public SearchClient disabledEsClient() {
        log.info("Elasticsearch is disabled (middleware.search.enabled=false)");
        return new DisabledSearchClientImpl();
    }

    /**
     * 搜索服务
     */
    @Bean
    public SearchService searchService(SearchClient searchClient) {
        return new SearchServiceImpl(searchClient, objectMapper);
    }
}
