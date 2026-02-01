package org.smm.archetype.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 缓存配置属性
 * @author Leonardo
 * @since 2026/1/10
 */
@Data
@ConfigurationProperties(prefix = "middleware.cache")
public class CacheProperties {

    /**
     * 初始容量
     */
    private Integer initialCapacity = 1000;

    /**
     * 最大容量
     */
    private Long maximumSize = 10000L;

    /**
     * 写入后过期时间
     */
    private Duration expireAfterWrite = Duration.ofDays(30);

    /**
     * 访问后过期时间
     */
    private Duration expireAfterAccess = Duration.ofDays(30);

}
