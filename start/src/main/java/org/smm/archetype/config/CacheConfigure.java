package org.smm.archetype.config;

import org.smm.archetype.config.properties.CacheProperties;
import org.smm.archetype.domain.bizshared.client.CacheClient;
import org.smm.archetype.infrastructure.bizshared.client.cache.impl.CaffeineCacheClientImpl;
import org.smm.archetype.infrastructure.bizshared.client.cache.impl.RedisCacheClientImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Infrastructure层缓存服务配置
 *
 * <p>Bean装配策略（自动依赖检测）：
 * <ul>
 *   <li>外部中间件优先：RedisTemplate存在时，使用@Primary标记的RedisCacheClientImpl</li>
 *   <li>本地兜底方案：RedisTemplate不存在时，使用CaffeineCacheClientImpl作为默认实现</li>
 *   <li>使用@ConditionalOnBean和@ConditionalOnMissingBean进行依赖自动检测</li>
 *   <li>SpringBoot自动配置RedisTemplate，无需手动创建</li>
 * </ul>
 *
 * <p>设计原则：
 * <ul>
 *   <li>启动时确定：所有中间件在应用启动时通过Bean装配确定</li>
 *   <li>无运行时切换：不支持运行时动态切换中间件</li>
 *   <li>本地兜底：即使没有外部中间件，应用也能正常运行</li>
 *   <li>去配置化：移除@ConditionalOnProperty类型选择，依赖自动检测</li>
 * </ul>
 * @author Leonardo
 * @since 2026-01-10
 */
@Configuration
@EnableConfigurationProperties(CacheProperties.class)
public class CacheConfigure {

    private final CacheProperties properties;

    public CacheConfigure(CacheProperties properties) {
        this.properties = properties;
    }

    /**
     * 本地组件：Caffeine缓存服务（默认实现）
     *
     * <p>作为兜底方案，当RedisTemplate不存在时才创建此Bean。
     *
     * <p>使用@ConditionalOnMissingBean确保Redis优先级更高（@Primary）。
     * @return Caffeine缓存服务实现
     */
    @Bean
    @ConditionalOnMissingBean(RedisTemplate.class)
    public CacheClient caffeineCacheService() {
        CacheProperties.Local local = properties.getLocal();
        return new CaffeineCacheClientImpl(
                local.getInitialCapacity(),
                local.getMaximumSize(),
                local.getExpireAfterWrite()
        );
    }

    /**
     * 外部中间件：Redis缓存服务
     *
     * <p>条件：当RedisTemplate Bean存在时才创建此Bean。
     *
     * <p>使用@Primary标记为优先Bean，自动覆盖Caffeine缓存。
     * @param redisTemplate Redis模板（由Spring Data Redis自动配置）
     * @return Redis缓存服务实现
     */
    @Bean
    @Primary
    @ConditionalOnBean(RedisTemplate.class)
    public CacheClient redisCacheService(final RedisTemplate<String, Object> redisTemplate) {
        return new RedisCacheClientImpl(redisTemplate);
    }

}
