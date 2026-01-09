package org.smm.archetype.adapter.access.web.config;

import org.smm.archetype.adapter.access.web.converter.OrderConverter;
import org.smm.archetype.adapter.access.web.filter.ContextFillFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Adapter层Web相关配置
 *
 * <p>负责创建Web层相关的Bean，如Controller、Filter、Converter等。
 * @author Leonardo
 * @since 2026-01-10
 */
@Configuration
public class AdapterWebConfig {

    /**
     * 订单转换器
     *
     * <p>负责订单相关的DTO转换。
     * @return 订单转换器
     */
    @Bean
    public OrderConverter orderConverter() {
        return new OrderConverter();
    }

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

}
