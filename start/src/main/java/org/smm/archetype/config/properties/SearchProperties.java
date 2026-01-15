package org.smm.archetype.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 搜索配置属性
 *
 * @author Leonardo
 * @since 2026-01-14
 */
@Data
@ConfigurationProperties(prefix = "middleware.search")
public class SearchProperties {

    /**
     * 是否启用搜索功能（默认：false）
     *
     * <p>false: 使用禁用实现，所有ES操作抛出异常（不强制ES依赖）
     * <p>true: 使用真实Elasticsearch，启动时会检查连接可用性
     */
    private boolean enabled = false;

    /**
     * Elasticsearch配置
     */
    private Elasticsearch elasticsearch = new Elasticsearch();

    /**
     * Elasticsearch配置
     */
    @Data
    public static class Elasticsearch {
        /**
         * ES端点
         *
         * <p>默认: http://localhost:9200
         */
        private String endpoint = "http://localhost:9200";

        /**
         * 用户名
         */
        private String username = "elastic";

        /**
         * 密码
         */
        private String password = "changeme";
    }

}
