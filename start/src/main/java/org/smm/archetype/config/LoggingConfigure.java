package org.smm.archetype.config;

import lombok.RequiredArgsConstructor;
import org.smm.archetype.config.logging.LoggingConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * 日志启动配置类
 *
 * <p>负责注册日志相关的 Spring Bean，包括：
 * <ul>
 *   <li>日志配置验证组件</li>
 * </ul>
 *
 * <p>⚠️ 配置类命名规范：必须遵循 `{Aggregate}Configure` 格式
 * <p>⚠️ Bean 装配规范：必须通过 `@Bean` 方法，禁止 `@Component` 扫描
 */
@Configuration
@RequiredArgsConstructor
public class LoggingConfigure {

    private final Environment environment;

    /**
     * 日志配置验证组件
     *
     * <p>在应用启动时验证日志配置的正确性，包括：
     * <ul>
     *   <li>日志路径配置检查</li>
     *   <li>日志目录存在性验证</li>
     *   <li>日志目录可写性验证</li>
     *   <li>权限问题检测和处理</li>
     * </ul>
     * @return LoggingConfiguration 实例
     */
    @Bean
    public LoggingConfiguration loggingConfiguration() {
        return new LoggingConfiguration(environment);
    }

}
