package org.smm.archetype.infrastructure.common.notification.config;

import org.smm.archetype.infrastructure.common.notification.email.AbstractEmailService;
import org.smm.archetype.infrastructure.common.notification.email.impl.TodoEmailServiceImpl;
import org.smm.archetype.infrastructure.common.notification.sms.AbstractSmsService;
import org.smm.archetype.infrastructure.common.notification.sms.impl.TodoSmsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Infrastructure层通知服务配置
 *
 * <p>负责创建邮件和短信服务的Bean。
 *
 * <p>当前状态：使用Todo实现，实际使用时需要替换为具体的服务商实现（阿里云、腾讯云等）。
 * @author Leonardo
 * @since 2026-01-10
 */
@Configuration
public class InfrastructureNotificationConfig {

    /**
     * Todo邮件服务实现
     *
     * <p>临时实现，抛出UnsupportedOperationException。
     * 实际使用时需要替换为具体的邮件服务实现（如阿里云邮件服务）。
     * @return Todo邮件服务
     */
    @Bean
    public AbstractEmailService todoEmailService() {
        return new TodoEmailServiceImpl();
    }

    /**
     * Todo短信服务实现
     *
     * <p>临时实现，抛出UnsupportedOperationException。
     * 实际使用时需要替换为具体的短信服务实现（如阿里云短信服务）。
     * @return Todo短信服务
     */
    @Bean
    public AbstractSmsService todoSmsService() {
        return new TodoSmsServiceImpl();
    }

}
