package org.smm.archetype.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 短信服务配置属性类。
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "middleware.sms")
public class SmsProperties {

    /**
     * 访问密钥ID
     */
    private String accessKeyId = "";

    /**
     * 访问密钥Secret
     */
    private String accessKeySecret = "";

    /**
     * 区域ID（默认：cn-hangzhou）
     */
    private String regionId = "cn-hangzhou";

    /**
     * 签名名称（默认：阿里云通信）
     */
    private String signName = "阿里云通信";

}
