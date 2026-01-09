package org.smm.archetype.infrastructure._shared.config;

import org.smm.archetype.infrastructure._shared.client.cache.impl.CaffeineCacheClientImpl;
import org.smm.archetype.infrastructure._shared.client.cache.impl.RedisCacheClientImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Infrastructure层缓存服务配置
 *
 * <p>Bean装配策略：
 * <ul>
 *   <li>本地组件：Caffeine缓存作为默认实现（兜底方案），总是会被创建</li>
 *   <li>外部中间件：Redis缓存在RedisTemplate存在时自动覆盖Caffeine</li>
 *   <li>使用@ConditionalOnBean检测外部中间件，使用@Primary标记优先级</li>
 * </ul>
 *
 * <p>设计原则：
 * <ul>
 *   <li>启动时确定：所有中间件在应用启动时通过Bean装配确定</li>
 *   <li>无运行时切换：不支持运行时动态切换中间件</li>
 *   <li>本地兜底：即使没有外部中间件，应用也能正常运行</li>
 * </ul>
 * @author Leonardo
 * @since 2026-01-10
 */
@Configuration
public class InfrastructureCacheConfig {

    /**
     * 本地组件：Caffeine缓存服务（默认实现）
     *
     * <p>作为兜底方案，总是会被创建。
     *
     * <p>只有在没有RedisTemplate时才会作为主Bean使用。
     * @return Caffeine缓存服务实现
     */
    @Bean
    public CaffeineCacheClientImpl caffeineCacheService() {
        return new CaffeineCacheClientImpl();
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
    @ConditionalOnBean(RedisTemplate.class)
    @Primary
    public RedisCacheClientImpl redisCacheService(final RedisTemplate<String, Object> redisTemplate) {
        return new RedisCacheClientImpl(redisTemplate);
    }

}
