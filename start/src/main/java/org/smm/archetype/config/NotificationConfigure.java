package org.smm.archetype.config;

import org.smm.archetype.config.properties.EmailProperties;
import org.smm.archetype.config.properties.SmsProperties;
import org.smm.archetype.infrastructure.bizshared.client.email.AliyunEmailClientImpl;
import org.smm.archetype.infrastructure.bizshared.client.sms.AliyunSmsClientImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云通知服务配置
 *
 * <p>通过@Bean方法显式注册阿里云通知服务Bean。
 *
 * <p>条件装配：
 * <ul>
 *   <li>短信服务：middleware.sms.type=aliyun 且配置了有效的访问密钥</li>
 *   <li>邮件服务：middleware.email.type=aliyun 且配置了有效的访问密钥</li>
 * </ul>
 *
 * @author Leonardo
 * @since 2026-01-10
 */
@Configuration
@EnableConfigurationProperties({
        SmsProperties.class,
        EmailProperties.class
})
public class NotificationConfigure {

    /**
     * 阿里云短信服务Bean
     *
     * <p>条件：
     * <ul>
     *   <li>middleware.sms.type=aliyun</li>
     *   <li>配置了有效的 access-key-id 和 access-key-secret</li>
     * </ul>
     *
     * @param smsProperties 短信配置属性
     * @return 阿里云短信服务实例
     */
    @Bean
    @ConditionalOnBooleanProperty("middleware.sms")
    public AliyunSmsClientImpl aliyunSmsService(SmsProperties smsProperties) {
        return new AliyunSmsClientImpl(
                smsProperties.getAccessKeyId(),
                smsProperties.getAccessKeySecret(),
                smsProperties.getRegionId(),
                smsProperties.getSignName()
        );
    }

    /**
     * 阿里云邮件服务Bean
     *
     * <p>条件：
     * <ul>
     *   <li>middleware.email.type=aliyun</li>
     *   <li>配置了有效的 access-key-id 和 access-key-secret</li>
     * </ul>
     *
     * @param emailProperties 邮件配置属性
     * @return 阿里云邮件服务实例
     */
    @Bean
    @ConditionalOnBooleanProperty("middleware.email")
    public AliyunEmailClientImpl aliyunEmailService(EmailProperties emailProperties) {
        return new AliyunEmailClientImpl(
                emailProperties.getAccessKeyId(),
                emailProperties.getAccessKeySecret(),
                emailProperties.getRegionId(),
                emailProperties.getFromAddress(),
                emailProperties.getFromAlias(),
                emailProperties.getAccountName()
        );
    }

}
