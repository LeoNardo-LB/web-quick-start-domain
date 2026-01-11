package org.smm.archetype.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 邮件服务配置属性
 *
 * <p>支持多种邮件服务提供商（阿里云、腾讯云等）
 * @author Leonardo
 * @since 2026-01-11
 */
@Getter
@ConfigurationProperties(prefix = "middleware.email")
public class EmailProperties {

    /**
     * 阿里云邮件配置
     */
    private final Aliyun aliyun = new Aliyun();
    /**
     * 邮件服务类型（aliyun、tencent等）
     */
    @Setter
    private       String type   = "aliyun";

    /**
     * 阿里云邮件服务配置
     */
    @Getter
    @Setter
    public static class Aliyun {

        /**
         * 访问密钥ID
         */
        private String accessKeyId;

        /**
         * 访问密钥Secret
         */
        private String accessKeySecret;

        /**
         * 区域ID（默认：cn-hangzhou）
         */
        private String regionId = "cn-hangzhou";

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
