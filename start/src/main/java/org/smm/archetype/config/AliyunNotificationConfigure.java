package org.smm.archetype.config;

import org.smm.archetype.infrastructure.common.notification.AliyunEmailServiceImpl;
import org.smm.archetype.infrastructure.common.notification.AliyunSmsServiceImpl;
import org.smm.archetype.infrastructure.config.properties.AliyunProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云通知服务配置
 *
 * <p>通过@Bean方法显式注册阿里云通知服务Bean。
 *
 * <p>条件装配：配置了aliyun.access-key-id时启用
 * @author Leonardo
 * @since 2026/01/10
 */
@Configuration
public class AliyunNotificationConfigure {

    /**
     * 阿里云短信服务Bean
     *
     * <p>条件：配置了aliyun.access-key-id时启用
     * @param aliyunProperties 阿里云配置属性
     * @return 阿里云短信服务实例
     */
    @Bean
    @ConditionalOnProperty(prefix = "aliyun", name = "access-key-id")
    public AliyunSmsServiceImpl aliyunSmsService(AliyunProperties aliyunProperties) {
        return new AliyunSmsServiceImpl(aliyunProperties);
    }

    /**
     * 阿里云邮件服务Bean
     *
     * <p>条件：配置了aliyun.access-key-id时启用
     * @param aliyunProperties 阿里云配置属性
     * @return 阿里云邮件服务实例
     */
    @Bean
    @ConditionalOnProperty(prefix = "aliyun", name = "access-key-id")
    public AliyunEmailServiceImpl aliyunEmailService(AliyunProperties aliyunProperties) {
        return new AliyunEmailServiceImpl(aliyunProperties);
    }

}
