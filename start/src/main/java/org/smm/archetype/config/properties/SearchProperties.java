package org.smm.archetype.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 搜索配置属性
 *
 * <p>搜索功能是否启用由 Spring Boot 自动配置决定：
 * <ul>
 *   <li>当 {@code ElasticsearchOperations} Bean 存在时，使用 Elasticsearch 搜索实现</li>
 *   <li>当 {@code ElasticsearchOperations} Bean 不存在时，使用禁用实现（所有操作抛出异常）</li>
 * </ul>
 *
 * <p>Elasticsearch 连接配置使用 Spring Boot 标准配置：
 * <pre>
 * spring.elasticsearch.uris: http://localhost:9200
 * spring.elasticsearch.username: elastic
 * spring.elasticsearch.password: changeme
 * </pre>
 *
 * @author Leonardo
 * @since 2026-01-14
 */
@Data
@ConfigurationProperties(prefix = "middleware.search")
public class SearchProperties {

}
