package org.smm.archetype.config;

import org.smm.archetype.domain._shared.client.IdClient;
import org.smm.archetype.infrastructure._shared.client.id.impl.SnowflakeIdClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ID服务配置
 *
 * <p>负责创建ID生成服务的Bean。
 * @author Leonardo
 * @since 2026-01-10
 */
@Configuration
public class IdConfigure {

    /**
     * Snowflake ID服务
     *
     * <p>基于Snowflake算法的分布式ID生成器。
     * @return Snowflake ID服务
     */
    @Bean
    public IdClient snowflakeIdService() {
        return new SnowflakeIdClient();
    }

}
