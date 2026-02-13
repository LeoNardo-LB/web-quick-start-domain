package org.smm.archetype.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 邮件服务配置属性类。
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "middleware.email")
public class EmailProperties {

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
     * 发信地址（例如：noreply@example.com）
     */
    private String fromAddress = "";

    /**
     * 发信人别名（可选）
     */
    private String fromAlias = "";

    /**
     * 账户名称（通常是发信地址）
     */
    private String accountName = "";

    /**
     * 回信地址（可选）
     */
    private String replyToAddress = "";

}
