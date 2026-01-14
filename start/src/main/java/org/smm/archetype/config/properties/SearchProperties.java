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
     * 搜索类型（elasticsearch | memory）
     *
     * <p>默认: elasticsearch
     */
    private String type = "elasticsearch";

    /**
     * Elasticsearch配置
     */
    private Elasticsearch elasticsearch = new Elasticsearch();

    /**
     * 内存ES配置
     */
    private Memory memory = new Memory();

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

    /**
     * 内存ES配置
     */
    @Data
    public static class Memory {
        /**
         * 数据路径
         *
         * <p>null表示纯内存，非null表示持久化到指定路径
         */
        private String dataPath = null;
    }
}
