package org.smm.archetype.infrastructure.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云服务配置属性
 * @author Leonardo
 * @since 2026/01/10
 */
@Component
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
    private String accessKeyId;
    /**
     * 访问密钥Secret
     */
    private String accessKeySecret;
    /**
     * 区域ID（默认：cn-hangzhou）
     */
    private String regionId = "cn-hangzhou";

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getRegionId() {
        return regionId;
    }

    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }

    public Sms getSms() {
        return sms;
    }

    public Email getEmail() {
        return email;
    }

    /**
     * 短信服务配置
     */
    @Data
    public static class Sms {

        /**
         * 签名名称（默认：阿里云通信）
         */
        private String signName = "阿里云通信";

    }

    /**
     * 邮件服务配置
     */
    @Data
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
