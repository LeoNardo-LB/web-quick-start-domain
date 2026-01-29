package org.smm.archetype.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.smm.archetype.infrastructure.common.log.LogAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Infrastructure层日志相关配置
 *
 * <p>负责创建日志切面、持久化、序列化等相关的Bean。
 * @author Leonardo
 * @since 2026-01-10
 */
@Configuration
public class LogConfigure {

    /**
     * 日志切面
     *
     * <p>拦截需要记录日志的方法，自动收集和持久化日志信息。
     * @param meterRegistry Micrometer指标注册中心
     * @return 日志切面
     */
    @Bean
    public LogAspect logAspect(MeterRegistry meterRegistry) {
        return new LogAspect(meterRegistry);
    }

}
