package org.smm.archetype.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 阿里云服务配置属性
 *
 * <p>注意：此配置类通过AliyunNotificationConfigure中的@EnableConfigurationProperties启用
 * @author Leonardo
 * @since 2026/01/10
 */
@Getter
@ConfigurationProperties(prefix = "aliyun")
public class AliyunProperties {

    /**
     * 短信服务配置
     */
    private final Sms sms = new Sms();

    /**
     * 邮件服务配置
     */
    private final Email email = new Email();

    /**
     * 访问密钥ID
     */
    @Setter
    private String accessKeyId;

    /**
     * 访问密钥Secret
     */
    @Setter
    private String accessKeySecret;

    /**
     * 区域ID（默认：cn-hangzhou）
     */
    @Setter
    private String regionId = "cn-hangzhou";

    /**
     * 短信服务配置
     */
    @Getter
    @Setter
    public static class Sms {

        /**
         * 签名名称（默认：阿里云通信）
         */
        private String signName = "阿里云通信";

    }

    /**
     * 邮件服务配置
     */
    @Getter
    @Setter
    public static class Email {

        /**
         * 发信地址（例如：noreply@example.com）
         */
        private String fromAddress;

        /**
         * 发信人别名（可选）
         */
        private String fromAlias;

        /**
         * 账户名称（通常是发信地址）
         */
        private String accountName;

        /**
         * 回信地址（可选）
         */
        private String replyToAddress;

    }

}
