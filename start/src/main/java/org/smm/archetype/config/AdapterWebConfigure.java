package org.smm.archetype.config;

import org.smm.archetype.adapter.access.web.aspect.LoggingAspect;
import org.smm.archetype.adapter.access.web.filter.ContextFillFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Adapter层Web相关配置
 *
 * <p>负责创建Web层相关的Bean，如Filter、Aspect等。
 * <p>注意：MapStruct Converter会自动生成为Spring Bean，无需手动注册。
 * @author Leonardo
 * @since 2026-01-10
 */
@Configuration
public class AdapterWebConfigure {

    /**
     * 上下文填充过滤器
     *
     * <p>为每个请求填充上下文信息，如用户ID、TraceId等。
     * @return 过滤器注册Bean
     */
    @Bean
    public FilterRegistrationBean<ContextFillFilter> contextFillFilter() {
        ContextFillFilter filter = new ContextFillFilter();
        FilterRegistrationBean<ContextFillFilter> registrationBean = new FilterRegistrationBean<>(filter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }

    /**
     * 日志切面
     *
     * <p>记录所有Controller方法的调用日志，包括请求信息、响应信息和耗时。
     * @return 日志切面Bean
     */
    @Bean
    public LoggingAspect loggingAspect() {
        return new LoggingAspect();
    }

}
