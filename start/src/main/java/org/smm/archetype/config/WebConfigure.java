package org.smm.archetype.config;

import org.smm.archetype.adapter.web.config.ContextFillFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Web层配置类，创建Filter和Web相关Bean。
 */
@Configuration
public class WebConfigure {

    /**
     * 上下文填充过滤器
     *
    为每个请求填充上下文信息，如用户ID、TraceId等。
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
