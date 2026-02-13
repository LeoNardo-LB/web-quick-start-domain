package org.smm.archetype.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.smm.archetype.infrastructure.shared.log.LogAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 日志配置类，创建日志切面和相关Bean。
 *
 * <p>⚠️ 重要提示：由于 Spring Boot 4.0.2 和 spring-boot-starter-aop 4.0.0-M2 存在兼容性问题，
 * LogAspect 的 AOP 功能当前不可用。此 Bean 创建仅用于维持配置结构。
 */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
public class LogConfigure {

    /**
     * 日志切面
     * 拦截需要记录日志的方法，自动收集和持久化日志信息。
     *
     * @param meterRegistry Micrometer指标注册表（可选，用于指标采集）
     * @return LogAspect 实例
     */
    @Bean
    public LogAspect logAspect(MeterRegistry meterRegistry) {
        return new LogAspect(meterRegistry);
    }

}
