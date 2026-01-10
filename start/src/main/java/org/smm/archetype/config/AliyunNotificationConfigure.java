package org.smm.archetype.config;

import org.smm.archetype.config.condition.AliyunEnabledCondition;
import org.smm.archetype.config.properties.AliyunProperties;
import org.smm.archetype.infrastructure.common.notification.AliyunEmailServiceImpl;
import org.smm.archetype.infrastructure.common.notification.AliyunSmsServiceImpl;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云通知服务配置
 *
 * <p>通过@Bean方法显式注册阿里云通知服务Bean。
 *
 * <p>条件装配：当且仅当配置了有效的阿里云访问密钥时才创建Bean
 * <ul>
 *   <li>access-key-id 必须配置且非占位符</li>
 *   <li>access-key-secret 必须配置且非占位符</li>
 * </ul>
 *
 * @author Leonardo
 * @since 2026/01/10
 */
@Configuration
@EnableConfigurationProperties(AliyunProperties.class)
public class AliyunNotificationConfigure {

    /**
     * 阿里云短信服务Bean
     *
     * <p>条件：配置了有效的aliyun.access-key-id和access-key-secret时启用
     * @param aliyunProperties 阿里云配置属性
     * @return 阿里云短信服务实例
     */
    @Bean
    @Conditional(AliyunEnabledCondition.class)
    public AliyunSmsServiceImpl aliyunSmsService(AliyunProperties aliyunProperties) {
        return new AliyunSmsServiceImpl(
                aliyunProperties.getAccessKeyId(),
                aliyunProperties.getAccessKeySecret(),
                aliyunProperties.getRegionId(),
                aliyunProperties.getSms().getSignName()
        );
    }

    /**
     * 阿里云邮件服务Bean
     *
     * <p>条件：配置了有效的aliyun.access-key-id和access-key-secret时启用
     * @param aliyunProperties 阿里云配置属性
     * @return 阿里云邮件服务实例
     */
    @Bean
    @Conditional(AliyunEnabledCondition.class)
    public AliyunEmailServiceImpl aliyunEmailService(AliyunProperties aliyunProperties) {
        return new AliyunEmailServiceImpl(
                aliyunProperties.getAccessKeyId(),
                aliyunProperties.getAccessKeySecret(),
                aliyunProperties.getRegionId(),
                aliyunProperties.getEmail().getFromAddress(),
                aliyunProperties.getEmail().getFromAlias(),
                aliyunProperties.getEmail().getAccountName(),
                aliyunProperties.getEmail().getReplyToAddress()
        );
    }

}
