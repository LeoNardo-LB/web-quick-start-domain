// package org.smm.archetype.config;
//
// import org.smm.archetype.config.properties.EmailProperties;
// import org.smm.archetype.config.properties.SmsProperties;
// import org.smm.archetype.infrastructure.shared.client.email.AliyunEmailClientImpl;
// import org.smm.archetype.infrastructure.shared.client.sms.AliyunSmsClientImpl;
// import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
// import org.springframework.boot.context.properties.EnableConfigurationProperties;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
//
// /**
//  * 阿里云通知服务配置类，注册短信和邮件服务Bean。
//  */
// @Configuration
// @EnableConfigurationProperties({
//         SmsProperties.class,
//         EmailProperties.class
// })
// public class NotificationConfigure {
//
//     /**
//      * 阿里云短信服务Bean
//      * @param smsProperties 短信配置属性
//      * @return 阿里云短信服务实例
//      */
//     @Bean
//     @ConditionalOnBooleanProperty("middleware.sms")
//     public AliyunSmsClientImpl aliyunSmsService(SmsProperties smsProperties) {
//         return new AliyunSmsClientImpl(
//                 smsProperties.getAccessKeyId(),
//                 smsProperties.getAccessKeySecret(),
//                 smsProperties.getRegionId(),
//                 smsProperties.getSignName()
//         );
//     }
//
//     /**
//      * 阿里云邮件服务Bean
//      * @param emailProperties 邮件配置属性
//      * @return 阿里云邮件服务实例
//      */
//     @Bean
//     @ConditionalOnBooleanProperty("middleware.email")
//     public AliyunEmailClientImpl aliyunEmailService(EmailProperties emailProperties) {
//         return new AliyunEmailClientImpl(
//                 emailProperties.getAccessKeyId(),
//                 emailProperties.getAccessKeySecret(),
//                 emailProperties.getRegionId(),
//                 emailProperties.getFromAddress(),
//                 emailProperties.getFromAlias(),
//                 emailProperties.getAccountName()
//         );
//     }
//
// }
