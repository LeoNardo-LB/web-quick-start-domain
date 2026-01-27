package org.smm.archetype.config;

import org.smm.archetype.domain.bizshared.client.EmailClient;
import org.smm.archetype.domain.bizshared.client.SmsClient;
import org.smm.archetype.infrastructure.bizshared.client.email.EmailClientImpl;
import org.smm.archetype.infrastructure.bizshared.client.sms.SmsClientImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
 *   <li>EmailClientImpl - 邮件服务（模拟实现）</li>
 *   <li>SmsClientImpl - 短信服务（模拟实现）</li>
 * </ul>
 *
 * <p>生产环境接入方式：
 * <ol>
 *   <li>添加邮件/短信服务SDK依赖</li>
 *   <li>实现AbstractEmailService和AbstractSmsService的子类</li>
 *   <li>使用@ConditionalOnMissingBean替换默认实现</li>
 *   <li>配置服务商账号信息</li>
 * </ol>
 *
 * <p>设计原则：
 * <ul>
 *   <li>Bean方法返回接口类型（EmailClient/SmsClient）而非具体实现</li>
 *   <li>使用@ConditionalOnMissingBean允许用户替换实现</li>
 *   <li>配置类位于 start/src/main/java/org/smm/archetype/config/</li>
 * </ul>
 *
 * @author Leonardo
 * @since 2026/01/10
 */
@Configuration
@Import(AliyunNotificationConfigure.class)
public class NotificationConfigure {

    /**
     * 短信服务Bean（模拟实现）
     * <p>职责：发送短信通知
     * <p>默认实现：SmsClientImpl（模拟实现，仅打印日志）
     * <p>生产环境：用户可以通过@ConditionalOnMissingBean替换为真实实现
     * @return SmsService接口类型
     */
    @Bean
    @ConditionalOnMissingBean(SmsClient.class)
    public SmsClient smsService() {
        return new SmsClientImpl();
    }

    /**
     * 邮件服务Bean（模拟实现）
     * <p>职责：发送邮件通知
     * <p>默认实现：EmailClientImpl（模拟实现，仅打印日志）
     * <p>生产环境：用户可以通过@ConditionalOnMissingBean替换为真实实现
     * @return EmailService接口类型
     */
    @Bean
    @ConditionalOnMissingBean(EmailClient.class)
    public EmailClient emailService() {
        return new EmailClientImpl();
    }

}
