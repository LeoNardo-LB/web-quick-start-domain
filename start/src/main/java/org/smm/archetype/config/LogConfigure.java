package org.smm.archetype.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.smm.archetype.infrastructure.common.log.LogAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 日志配置类，创建日志切面和相关Bean。
 */
@Configuration
public class LogConfigure {

    /**
     * 日志切面
     *
    拦截需要记录日志的方法，自动收集和持久化日志信息。
     * @param meterRegistry Micrometer指标注册中心
     * @return 日志切面
     */
    @Bean
    public LogAspect logAspect(MeterRegistry meterRegistry) {
        return new LogAspect(meterRegistry);
    }

}
