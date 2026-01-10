package org.smm.archetype.config;

import org.springframework.context.annotation.Configuration;

/**
 * Infrastructure层通知服务配置
 *
 * <p>通知服务的Bean由@Service注解自动注册，不需要手动配置。
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
 *   <li>添加@Service注解或创建配置类通过@Bean注册</li>
 *   <li>配置服务商账号信息</li>
 * </ol>
 *
 * @author Leonardo
 * @since 2026/01/10
 */
@Configuration
public class NotificationConfigure {

    // 通知服务Bean由@Service注解自动注册
    // EmailServiceImpl和SmsServiceImpl已经在各自类中添加@Service注解

}
