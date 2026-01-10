package org.smm.archetype.config;

import org.smm.archetype.infrastructure.common.notification.EmailServiceImpl;
import org.smm.archetype.infrastructure.common.notification.SmsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Infrastructure层通知服务配置
 *
 * <p>负责创建通知服务相关的Bean。
 *
 * <p>当前实现：
 * <ul>
 *   <li>EmailServiceImpl - 邮件服务（模拟实现）</li>
 *   <li>SmsServiceImpl - 短信服务（模拟实现）</li>
 * </ul>
 *
 * <p>生产环境接入方式：
 * <ol>
 *   <li>添加邮件/短信服务SDK依赖</li>
 *   <li>实现AbstractEmailService和AbstractSmsService的子类</li>
 *   <li>在此配置类中通过@Bean注册新实现</li>
 *   <li>配置服务商账号信息</li>
 * </ol>
 *
 * @author Leonardo
 * @since 2026/01/10
 */
@Configuration
@Import(AliyunNotificationConfigure.class)
public class NotificationConfigure {

    /**
     * 短信服务Bean（模拟实现）
     * @return 短信服务实例
     */
    @Bean
    public SmsServiceImpl smsService() {
        return new SmsServiceImpl();
    }

    /**
     * 邮件服务Bean（模拟实现）
     * @return 邮件服务实例
     */
    @Bean
    public EmailServiceImpl emailService() {
        return new EmailServiceImpl();
    }

}
