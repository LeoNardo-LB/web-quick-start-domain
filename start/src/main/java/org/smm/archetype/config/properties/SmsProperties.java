package org.smm.archetype.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 短信服务配置属性
 *
 * <p>支持多种短信服务提供商（阿里云、腾讯云等）
 * @author Leonardo
 * @since 2026-01-11
 */
@Getter
@ConfigurationProperties(prefix = "middleware.sms")
public class SmsProperties {

    /**
     * 阿里云短信配置
     */
    private final Aliyun aliyun = new Aliyun();
    /**
     * 短信服务类型（aliyun、tencent等）
     */
    @Setter
    private       String type   = "aliyun";

    /**
     * 阿里云短信服务配置
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
         * 签名名称（默认：阿里云通信）
         */
        private String signName = "阿里云通信";

    }

}
